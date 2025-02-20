package hr.ekufrin.training.generics;

import hr.ekufrin.training.exception.ConnectionToDatabaseException;

import java.util.Set;

/**
 * Abstract class that represents a repository.
 * @param <T> - type of entity
 */
public abstract class Repository <T> {
    /**
     * Saves entity to database.
     * @param entity - entity to save
     * @throws ConnectionToDatabaseException - if there is an error with connection to database
     */
    public abstract void save(T entity) throws ConnectionToDatabaseException;

    /**
     * Deletes entity in database.
     * @param entity - entity to delete
     * @throws ConnectionToDatabaseException - if there is an error with connection to database
     */
    public abstract void delete(T entity) throws ConnectionToDatabaseException;

    /**
     * Finds entity by id.
     * @param id - id of entity
     * @return entity with given id
     * @throws ConnectionToDatabaseException - if there is an error with connection to database
     */
    public abstract T findById(Long id) throws ConnectionToDatabaseException;

    /**
     * Finds all entities.
     * @return set of all entities
     * @throws ConnectionToDatabaseException - if there is an error with connection to database
     */
    public abstract Set<T> findAll() throws ConnectionToDatabaseException;
}
