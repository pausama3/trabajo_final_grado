package com.medmanager.ServidorMedManager.dao;

import java.util.List;

/**
 * Contrato genérico del patrón DAO (Data Access Object) con las operaciones
 * CRUD básicas que comparten todas las entidades.
 *
 * @param <T> tipo de entidad que gestiona el DAO
 */
public interface IDAO<T> {

    /** Inserta una entidad nueva en la base de datos. */
    void save(T entity);

    /** Actualiza una entidad ya existente. */
    void update(T entity);

    /**
     * @param id clave primaria
     * @return la entidad con ese id, o {@code null} si no existe
     */
    T findById(int id);

    /** @return todas las entidades de este tipo */
    List<T> findAll();
}