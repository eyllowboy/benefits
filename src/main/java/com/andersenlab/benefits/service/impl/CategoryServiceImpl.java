package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.CategoryEntity;
import com.andersenlab.benefits.repository.CategoryRepository;
import com.andersenlab.benefits.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.andersenlab.benefits.service.impl.ValidateUtils.validateEntityFieldsAnnotations;

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
    public CategoryServiceImpl(final CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Optional<CategoryEntity> findByTitle(final String title) {
        return this.categoryRepository.findByTitle(title);
    }

    @Override
    @Transactional
    public void updateCategoryEntity(final Long id, final String title) {
        final CategoryEntity validatedCategory = new CategoryEntity(id, title);
        validateEntityFieldsAnnotations(validatedCategory, false);
        this.categoryRepository.updateCategoryEntity(validatedCategory.getId(), validatedCategory.getTitle());
    }

    @Override
    public Page<CategoryEntity> findAll(final Pageable pageable) {
        return this.categoryRepository.findAll(pageable);
    }

    @Override
    public CategoryEntity findById(final Long id) {
        return this.categoryRepository.findById(id).orElseThrow(
                () -> new IllegalStateException("Category with this id was not found in the database"));
    }

    @Override
    public CategoryEntity save(final CategoryEntity entity) {
        entity.setId(null);
        validateEntityFieldsAnnotations(entity, true);
        return this.categoryRepository.save(entity);
    }

    @Override
    public void delete(final Long id) {
        this.categoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Optional<CategoryEntity> findWithAssociatedDiscounts(final Long id) {
        return this.categoryRepository.findWithAssociatedDiscounts(id);
    }
}
