package com.barquito.caja.infrastructure;

import com.barquito.caja.application.MesaLiberarPort;
import com.barquito.caja.domain.VentaOperacionInvalidaException;
import com.barquito.mesas.application.MesaService;
import com.barquito.mesas.domain.EstadoMesa;
import com.barquito.mesas.domain.MesaOperacionInvalidaException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de salida que implementa {@link MesaLiberarPort}.
 *
 * <p>Adquiere un lock {@code PESSIMISTIC_WRITE} sobre la fila de mesa antes de
 * realizar la transición {@code CUENTA_PEDIDA → DISPONIBLE}.
 * Requiere una transacción activa del llamador ({@code Propagation.MANDATORY}).
 *
 * <p>Traduce {@link MesaOperacionInvalidaException} (vocabulario de mesas) a
 * {@link VentaOperacionInvalidaException} (vocabulario de caja) para mantener
 * la consistencia de la superficie API del módulo caja.
 */
@Component
public class MesaLiberarAdapter implements MesaLiberarPort {

    private final MesaService mesaService;
    private final EntityManager em;

    /**
     * Construye el adaptador con sus dependencias.
     *
     * @param mesaService servicio de mesas para transiciones de estado.
     * @param em          EntityManager para adquirir el lock PESSIMISTIC_WRITE.
     */
    public MesaLiberarAdapter(final MesaService mesaService, final EntityManager em) {
        this.mesaService = mesaService;
        this.em = em;
    }

    /**
     * {@inheritDoc}
     *
     * @throws VentaOperacionInvalidaException si la mesa no está en CUENTA_PEDIDA.
     * @throws org.springframework.transaction.IllegalTransactionStateException si no hay TX activa.
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void liberarMesa(final Long mesaId) {
        em.find(com.barquito.mesas.infrastructure.MesaEntity.class, mesaId,
                LockModeType.PESSIMISTIC_WRITE);
        try {
            mesaService.cambiarEstado(mesaId, EstadoMesa.DISPONIBLE);
        } catch (final MesaOperacionInvalidaException ex) {
            // Anti-corruption: traducir al vocabulario de caja para que el handler HTTP sea consistente
            throw new VentaOperacionInvalidaException(
                    "No se pudo liberar la mesa " + mesaId + ": " + ex.getMessage());
        }
    }
}
