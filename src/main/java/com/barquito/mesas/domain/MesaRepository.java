package com.barquito.mesas.domain;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (output port) del dominio de mesas.
 *
 * <p>Define el contrato de acceso a datos para mesas sin acoplarse a ningún
 * framework de persistencia. La implementación concreta vive en la capa de
 * infraestructura ({@code MesaJpaAdapter}).
 */
public interface MesaRepository {

    /**
     * Guarda una mesa (creación o actualización).
     *
     * @param mesa la mesa a persistir.
     * @return la mesa persistida, con el id asignado si es nueva.
     */
    Mesa save(Mesa mesa);

    /**
     * Busca una mesa por su id.
     *
     * @param id identificador de la mesa.
     * @return un {@link Optional} con la mesa si existe, vacío si no.
     */
    Optional<Mesa> findById(Long id);

    /**
     * Retorna todas las mesas activas (independientemente del estado).
     *
     * <p>Incluye mesas en estado {@link EstadoMesa#FUSIONADA}.
     *
     * @return lista de mesas con {@code activa = true}.
     */
    List<Mesa> findAllActivas();

    /**
     * Retorna todas las mesas activas de una zona específica.
     *
     * @param zonaId id de la zona.
     * @return lista de mesas activas en la zona.
     */
    List<Mesa> findAllActivasByZonaId(Long zonaId);

    /**
     * Retorna las mesas secundarias activas de una mesa principal.
     *
     * <p>Usado para verificar que no existen secundarias antes de desactivar una principal.
     *
     * @param mesaPrincipalId id de la mesa principal.
     * @return lista de mesas secundarias activas.
     */
    List<Mesa> findSecundariasByMesaPrincipalId(Long mesaPrincipalId);
}
