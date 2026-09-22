package ru.eventagency.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T> {
    T create(T entity);
    List<T> findAll();
    Optional<T> findById(int id);
    void update(T entity);
    void delete(int id);
}
