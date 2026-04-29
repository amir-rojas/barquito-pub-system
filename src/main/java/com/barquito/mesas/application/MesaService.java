package com.barquito.mesas.application;

import com.barquito.mesas.domain.EstadoMesa;
import com.barquito.mesas.domain.FormaMesa;
import com.barquito.mesas.domain.Mesa;

import java.util.List;

/**
 * Puerto de entrada (input port) para operaciones sobre mesas.
 *
 * <p>Define el contrato que expone la capa de aplicación al mundo exterior.
 * La implementación concreta es {@code MesaServiceImpl}.
 */
public interface MesaService {

    /**
     * Crea una nueva mesa.
     *
     * @param numero  identificador visible (único).
     * @param zonaId  id de la zona a la que pertenece.
     * @param forma   forma física (puede ser null).
     * @return la mesa creada con id asignado.
     */
    Mesa crearMesa(String numero, Long zonaId, FormaMesa forma);

    /**
     * Busca una mesa por su identificador.
     *
     * @param id identificador de la mesa.
     * @return la mesa encontrada.
     * @throws com.barquito.mesas.domain.MesaNotFoundException si no existe ninguna mesa con ese id.
     */
    Mesa buscarMesa(Long id);

    /**
     * Retorna todas las mesas activas (incluyendo FUSIONADA).
     *
     * @return lista de mesas activas.
     */
    List<Mesa> listarMesasActivas();

    /**
     * Retorna todas las mesas activas de una zona.
     *
     * @param zonaId id de la zona.
     * @return lista de mesas activas en la zona.
     */
    List<Mesa> listarMesasPorZona(Long zonaId);

    /**
     * Actualiza los atributos físicos de una mesa.
     *
     * @param id     id de la mesa.
     * @param numero nuevo número (puede ser null para no cambiar).
     * @param zonaId nuevo id de zona (puede ser null para no cambiar).
     * @param forma  nueva forma (puede ser null para no cambiar).
     * @return la mesa actualizada.
     */
    Mesa actualizarMesa(Long id, String numero, Long zonaId, FormaMesa forma);

    /**
     * Cambia el estado operativo de una mesa.
     *
     * <p>No acepta {@link EstadoMesa#FUSIONADA} como estado destino
     * (debe usarse {@link #fusionarMesa(Long, Long)}).
     *
     * @param id          id de la mesa.
     * @param nuevoEstado nuevo estado (DISPONIBLE, OCUPADA o CUENTA_PEDIDA).
     * @return la mesa actualizada.
     */
    Mesa cambiarEstado(Long id, EstadoMesa nuevoEstado);

    /**
     * Activa o desactiva una mesa (soft delete).
     *
     * <p>Para desactivar, la mesa debe estar en estado {@link EstadoMesa#DISPONIBLE},
     * no estar {@link EstadoMesa#FUSIONADA} y no tener secundarias activas.
     *
     * @param id     id de la mesa.
     * @param activa nuevo valor.
     * @return la mesa actualizada.
     */
    Mesa cambiarActiva(Long id, boolean activa);

    /**
     * Fusiona una mesa secundaria bajo una mesa principal.
     *
     * <p>La secundaria pasa al estado {@link EstadoMesa#FUSIONADA} y se asocia
     * a la principal mediante {@code mesaPrincipalId}.
     *
     * @param principalId   id de la mesa principal.
     * @param secundariaId  id de la mesa secundaria.
     * @return la mesa secundaria en estado FUSIONADA.
     */
    Mesa fusionarMesa(Long principalId, Long secundariaId);

    /**
     * Deshace la fusión de una mesa secundaria.
     *
     * <p>La secundaria vuelve al estado {@link EstadoMesa#DISPONIBLE} y
     * {@code mesaPrincipalId} se pone a null.
     *
     * @param secundariaId id de la mesa secundaria (actualmente FUSIONADA).
     * @return la mesa secundaria en estado DISPONIBLE.
     */
    Mesa desfusionarMesa(Long secundariaId);
}
