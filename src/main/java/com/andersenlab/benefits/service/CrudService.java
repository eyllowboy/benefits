package com.andersenlab.benefits.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface CrudService<T> {
	List<T> findAll();
	
	Optional<T> findById(final Long id);
	
	T save(final T entity);
	
	void delete(final Long id);
}