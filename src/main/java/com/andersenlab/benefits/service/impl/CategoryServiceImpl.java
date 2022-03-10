package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.CategoryEntity;
import com.andersenlab.benefits.repository.CategoryRepository;
import com.andersenlab.benefits.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/***
 * Implementation for performing operations on a {@link CategoryEntity}
 * @author Denis Popov
 * @version 1.0
 * @see CategoryService
 * @see CategoryEntity
 */
@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Optional<CategoryEntity> findByTitle(String title) {
        return categoryRepository.findByTitle(title);
    }

    @Override
    public void updateCategoryEntity(Long id, String title) {
        categoryRepository.updateCategoryEntity(id, title);
    }

    @Override
    public List<CategoryEntity> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<CategoryEntity> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public CategoryEntity save(CategoryEntity entity) {
        return categoryRepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public Optional<CategoryEntity> findWithAssociatedDiscounts(final Long id) {
        return categoryRepository.findWithAssociatedDiscounts(id);
    };
}
