package com.barquito.pedidos.infrastructure;

import com.barquito.mesas.application.MesaService;
import com.barquito.mesas.domain.EstadoMesa;
import com.barquito.pedidos.application.MesaStatusPort;
import com.barquito.pedidos.domain.PedidoOperacionInvalidaException;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de salida que implementa {@link MesaStatusPort}.
 *
 * <p>Adquiere un lock {@code PESSIMISTIC_WRITE} sobre la fila de mesa antes de
 * realizar cualquier transición de estado. Requiere una transacción activa del
 * llamador ({@code Propagation.MANDATORY}).
 *
 * <p>Este es el ÚNICO lugar del módulo de pedidos que importa clases del módulo de mesas.
 */
@Component
public class MesaStatusAdapter implements MesaStatusPort {

    private final MesaService mesaService;
    private final EntityManager em;

    /**
     * Construye el adaptador con sus dependencias.
     *
     * @param mesaService servicio de mesas para transiciones de estado.
     * @param em          EntityManager para adquirir el lock PESSIMISTIC_WRITE.
     */
    public MesaStatusAdapter(final MesaService mesaService, final EntityManager em) {
        this.mesaService = mesaService;
        this.em = em;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void ocupar(final Long mesaId) {
        em.createNativeQuery("SELECT 1 FROM mesas WHERE id = :id FOR UPDATE")
                .setParameter("id", mesaId)
                .getSingleResult();
        final com.barquito.mesas.domain.Mesa mesa = mesaService.buscarMesa(mesaId);
        // re-acquire lock after fetch to ensure consistent state check
        if (mesa.estado() == EstadoMesa.FUSIONADA) {
            throw new PedidoOperacionInvalidaException(
                    "No se puede crear un pedido en una mesa FUSIONADA. Mesa id: " + mesaId);
        }
        if (mesa.estado() == EstadoMesa.CUENTA_PEDIDA) {
            throw new PedidoOperacionInvalidaException(
                    "No se puede crear un pedido en una mesa en CUENTA_PEDIDA. Mesa id: " + mesaId);
        }
        if (!mesa.activa()) {
            throw new PedidoOperacionInvalidaException(
                    "No se puede crear un pedido en una mesa inactiva. Mesa id: " + mesaId);
        }
        mesaService.cambiarEstado(mesaId, EstadoMesa.OCUPADA);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void transicionarACuentaPedida(final Long mesaId) {
        em.createNativeQuery("SELECT 1 FROM mesas WHERE id = :id FOR UPDATE")
                .setParameter("id", mesaId)
                .getSingleResult();
        mesaService.cambiarEstado(mesaId, EstadoMesa.CUENTA_PEDIDA);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void liberarMesa(final Long mesaId) {
        em.createNativeQuery("SELECT 1 FROM mesas WHERE id = :id FOR UPDATE")
                .setParameter("id", mesaId)
                .getSingleResult();
        mesaService.cambiarEstado(mesaId, EstadoMesa.DISPONIBLE);
    }
}
