package com.barquito.mesas.application;

import com.barquito.mesas.domain.EstadoMesa;
import com.barquito.mesas.domain.FormaMesa;
import com.barquito.mesas.domain.Mesa;
import com.barquito.mesas.domain.MesaNotFoundException;
import com.barquito.mesas.domain.MesaOperacionInvalidaException;
import com.barquito.mesas.domain.MesaRepository;
import com.barquito.mesas.domain.ZonaNotFoundException;
import com.barquito.mesas.domain.ZonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del caso de uso de gestión de mesas.
 *
 * <p>Aplica todas las invariantes de negocio antes de persistir:
 * <ul>
 *   <li>{@link #cambiarEstado}: rechaza FUSIONADA como destino; rechaza mesa que ya es FUSIONADA.</li>
 *   <li>{@link #cambiarActiva}(false): la mesa debe estar DISPONIBLE, no ser FUSIONADA y
 *       no tener secundarias activas.</li>
 *   <li>{@link #fusionarMesa}: no auto-fusión, principal no puede ser FUSIONADA,
 *       secundaria debe ser DISPONIBLE y sin principal ya asignado, no ciclos.</li>
 *   <li>{@link #desfusionarMesa}: la mesa debe estar en estado FUSIONADA.</li>
 * </ul>
 */
@Service
@Transactional
public class MesaServiceImpl implements MesaService {

    private final MesaRepository mesaRepository;
    private final ZonaRepository zonaRepository;

    /**
     * Construye el servicio con los repositorios necesarios.
     *
     * @param mesaRepository puerto de salida para persistencia de mesas.
     * @param zonaRepository puerto de salida para consulta de zonas.
     */
    public MesaServiceImpl(final MesaRepository mesaRepository,
                           final ZonaRepository zonaRepository) {
        this.mesaRepository = mesaRepository;
        this.zonaRepository = zonaRepository;
    }

    @Override
    public Mesa crearMesa(final String numero, final Long zonaId, final FormaMesa forma) {
        zonaRepository.findById(zonaId)
                .orElseThrow(() -> new ZonaNotFoundException(zonaId));
        final Mesa nueva = new Mesa(null, numero, EstadoMesa.DISPONIBLE, true, zonaId, forma, null);
        return mesaRepository.save(nueva);
    }

    @Override
    @Transactional(readOnly = true)
    public Mesa buscarMesa(final Long id) {
        return mesaRepository.findById(id)
                .orElseThrow(() -> new MesaNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mesa> listarMesasActivas() {
        return mesaRepository.findAllActivas();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mesa> listarMesasPorZona(final Long zonaId) {
        return mesaRepository.findAllActivasByZonaId(zonaId);
    }

    @Override
    public Mesa actualizarMesa(final Long id, final String numero,
                               final Long zonaId, final FormaMesa forma) {
        final Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new MesaNotFoundException(id));
        if (zonaId != null) {
            zonaRepository.findById(zonaId)
                    .orElseThrow(() -> new ZonaNotFoundException(zonaId));
        }
        return mesaRepository.save(mesa.conAtributosFisicos(numero, forma, zonaId));
    }

    @Override
    public Mesa cambiarEstado(final Long id, final EstadoMesa nuevoEstado) {
        final Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new MesaNotFoundException(id));
        if (nuevoEstado == EstadoMesa.FUSIONADA) {
            throw new MesaOperacionInvalidaException(
                    "No se puede asignar el estado FUSIONADA directamente. "
                            + "Use el endpoint de fusión.");
        }
        if (mesa.estado() == EstadoMesa.FUSIONADA) {
            throw new MesaOperacionInvalidaException(
                    "No se puede cambiar el estado de una mesa FUSIONADA. "
                            + "Desfusiónela primero.");
        }
        return mesaRepository.save(mesa.conEstado(nuevoEstado));
    }

    @Override
    public Mesa cambiarActiva(final Long id, final boolean activa) {
        final Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new MesaNotFoundException(id));
        if (!activa) {
            if (mesa.esFusionada()) {
                throw new MesaOperacionInvalidaException(
                        "No se puede desactivar una mesa que está fusionada como secundaria.");
            }
            if (mesa.estado() != EstadoMesa.DISPONIBLE) {
                throw new MesaOperacionInvalidaException(
                        "Solo se puede desactivar una mesa en estado DISPONIBLE. "
                                + "Estado actual: " + mesa.estado());
            }
            final List<Mesa> secundarias = mesaRepository.findSecundariasByMesaPrincipalId(id);
            if (!secundarias.isEmpty()) {
                throw new MesaOperacionInvalidaException(
                        "No se puede desactivar una mesa principal con secundarias activas.");
            }
        }
        return mesaRepository.save(mesa.conActiva(activa));
    }

    @Override
    public Mesa fusionarMesa(final Long principalId, final Long secundariaId) {
        if (principalId.equals(secundariaId)) {
            throw new MesaOperacionInvalidaException(
                    "Una mesa no puede fusionarse consigo misma.");
        }
        final Mesa principal = mesaRepository.findById(principalId)
                .orElseThrow(() -> new MesaNotFoundException(principalId));
        final Mesa secundaria = mesaRepository.findById(secundariaId)
                .orElseThrow(() -> new MesaNotFoundException(secundariaId));

        if (!principal.puedeSerPrincipal()) {
            throw new MesaOperacionInvalidaException(
                    "La mesa " + principalId + " no puede actuar como principal. "
                            + "Estado: " + principal.estado()
                            + ", esFusionada: " + principal.esFusionada());
        }
        if (!secundaria.puedeSerFusionadaComoSecundaria()) {
            throw new MesaOperacionInvalidaException(
                    "La mesa " + secundariaId + " no puede fusionarse como secundaria. "
                            + "Estado: " + secundaria.estado()
                            + ", esFusionada: " + secundaria.esFusionada());
        }
        // Anti-ciclo: si la principal ya apunta a la secundaria como su principal → ciclo
        if (secundariaId.equals(principal.mesaPrincipalId())) {
            throw new MesaOperacionInvalidaException(
                    "Ciclo detectado: la mesa " + principalId
                            + " ya es secundaria de la mesa " + secundariaId);
        }

        final Mesa fusionada = new Mesa(
                secundariaId,
                secundaria.numero(),
                EstadoMesa.FUSIONADA,
                secundaria.activa(),
                secundaria.zonaId(),
                secundaria.forma(),
                principalId
        );
        return mesaRepository.save(fusionada);
    }

    @Override
    public Mesa desfusionarMesa(final Long secundariaId) {
        final Mesa mesa = mesaRepository.findById(secundariaId)
                .orElseThrow(() -> new MesaNotFoundException(secundariaId));
        if (mesa.estado() != EstadoMesa.FUSIONADA) {
            throw new MesaOperacionInvalidaException(
                    "Solo se puede desfusionar una mesa en estado FUSIONADA. "
                            + "Estado actual: " + mesa.estado());
        }
        final Mesa desfusionada = new Mesa(
                mesa.id(),
                mesa.numero(),
                EstadoMesa.DISPONIBLE,
                mesa.activa(),
                mesa.zonaId(),
                mesa.forma(),
                null
        );
        return mesaRepository.save(desfusionada);
    }
}
