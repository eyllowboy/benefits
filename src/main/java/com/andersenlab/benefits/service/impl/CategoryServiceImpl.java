package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.CategoryEntity;
import com.andersenlab.benefits.repository.CategoryRepository;
import com.andersenlab.benefits.service.CategoryService;
import org.springframework.beans.BeanUtils;
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
    public CategoryEntity update(final Long id, final CategoryEntity category) {
        final Optional<CategoryEntity> theSameTitle = this.findByTitle(category.getTitle());
        if (theSameTitle.isPresent() && !theSameTitle.get().getId().equals(id))
            throw new IllegalStateException("Category with title '" + category.getTitle() + "' already exists");
        final CategoryEntity existingCategory = this.findById(id);
        BeanUtils.copyProperties(category, existingCategory, "id");
        final CategoryEntity validatedCategory = new CategoryEntity(id, existingCategory.getTitle());
        validateEntityFieldsAnnotations(validatedCategory, false);
        this.categoryRepository.updateCategoryEntity(validatedCategory.getId(), validatedCategory.getTitle());
        return validatedCategory;
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
    @Transactional
    public CategoryEntity save(final CategoryEntity category) {
        this.findByTitle(category.getTitle()).ifPresent(categoryEntity -> {
            throw new IllegalStateException("Category with title '" + category.getTitle() + "' already exists");
        });
        category.setId(null);
        validateEntityFieldsAnnotations(category, true);
        return this.categoryRepository.save(category);
    }

    @Override
    public void delete(final Long id) {
        this.findById(id);
        final Optional<CategoryEntity> categoryEntity = this.findWithAssociatedDiscounts(id);
        if (categoryEntity.isPresent() && categoryEntity.get().getDiscounts().size() > 0)
            throw new IllegalStateException("There is active discounts in this Category in database");
        this.categoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Optional<CategoryEntity> findWithAssociatedDiscounts(final Long id) {
        return this.categoryRepository.findWithAssociatedDiscounts(id);
    }
}
