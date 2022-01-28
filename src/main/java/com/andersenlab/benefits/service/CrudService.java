package com.andersenlab.benefits.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface CrudService<T> {
	List<T> findAll();
	
	Optional<T> findById(Long id);
	
	T save(T entity);
	
	void delete(Long id);
}