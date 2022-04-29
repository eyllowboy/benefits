package com.andersenlab.benefits.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Basic interface for performing СRUD operations on database entities.
 * @author Andrei Rabchun
 * @version 1.0
 */
@Service
public interface CrudService<T> {
	/**
	 * @return the page of entities from database, error if not processed
	 */
	Page<T> findAll(final Pageable pageable);
	/**
	 *
	 * @param id  the id of record in the database, not null
	 * @return the entity corresponding given id from database, error if id not found
	 */
	T findById(final Long id);
	/**
	 * @param entity contains information to create a new record in the database, not null
	 * @return the entity corresponding new record in the database, error if consistency conditions are not met
	 */
	T save(final T entity);

	/**
	 * @param id of entity to be updated
	 * @param entity in which fields with not-null values need to be updated
	 * @return updated entity
	 */
	T update(final Long id, final T entity);

	/**
	 * @param id  the id of record in the database, not null
	 */
	void delete(final Long id);
}