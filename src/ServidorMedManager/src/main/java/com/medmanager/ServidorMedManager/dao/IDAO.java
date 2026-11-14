package com.medmanager.ServidorMedManager.dao;

import java.util.List;

public interface IDAO<T> {
    void save(T entity);
    void update(T entity);
    T findById(int id);
    List<T> findAll();
}
