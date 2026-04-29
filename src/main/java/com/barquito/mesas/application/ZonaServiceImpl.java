package com.barquito.mesas.application;

import com.barquito.mesas.domain.MesaOperacionInvalidaException;
import com.barquito.mesas.domain.Zona;
import com.barquito.mesas.domain.ZonaNotFoundException;
import com.barquito.mesas.domain.ZonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del caso de uso de gestión de zonas.
 *
 * <p>Verifica unicidad de nombre (case-insensitive) antes de guardar.
 * Lanza {@link ZonaNotFoundException} si el id no existe al actualizar.
 * Lanza {@link MesaOperacionInvalidaException} si el nombre está duplicado.
 */
@Service
@Transactional
public class ZonaServiceImpl implements ZonaService {

    private final ZonaRepository zonaRepository;

    /**
     * Construye el servicio con el repositorio de zonas.
     *
     * @param zonaRepository puerto de salida para persistencia de zonas.
     */
    public ZonaServiceImpl(final ZonaRepository zonaRepository) {
        this.zonaRepository = zonaRepository;
    }

    @Override
    public Zona crearZona(final String nombre, final String descripcion, final int orden) {
        if (zonaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new MesaOperacionInvalidaException(
                    "Ya existe una zona con el nombre '" + nombre + "'");
        }
        final Zona nueva = new Zona(null, nombre, descripcion, orden);
        return zonaRepository.save(nueva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Zona> listarZonas() {
        return zonaRepository.findAllOrdenadas();
    }

    @Override
    public Zona actualizarZona(final Long id, final String nombre,
                               final String descripcion, final int orden) {
        zonaRepository.findById(id)
                .orElseThrow(() -> new ZonaNotFoundException(id));
        if (zonaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new MesaOperacionInvalidaException(
                    "Ya existe una zona con el nombre '" + nombre + "'");
        }
        final Zona actualizada = new Zona(id, nombre, descripcion, orden);
        return zonaRepository.save(actualizada);
    }
}
