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
import java.util.Optional;
import static com.andersenlab.benefits.service.impl.ValidateUtils.*;

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

    @Override
    @Transactional
    public CategoryEntity update(final Long id, final CategoryEntity categoryEntity) {
        final Optional<CategoryEntity> theSameTitle = this.categoryRepository.findByTitle(categoryEntity.getTitle());
        if (theSameTitle.isPresent() && !theSameTitle.get().getId().equals(id))
            throw new IllegalStateException(errAlreadyExistMessage("category", "category title", categoryEntity.getTitle()));
        final CategoryEntity existingCategory = findById(id);
        BeanUtils.copyProperties(categoryEntity, existingCategory, "id");
        final CategoryEntity validatedCategory = new CategoryEntity(id, existingCategory.getTitle());
        validateEntityFieldsAnnotations(validatedCategory, false);
        return this.categoryRepository.save(existingCategory);
    }

    @Override
    public Page<CategoryEntity> findAll(final Pageable pageable) {
        return this.categoryRepository.findAll(pageable);
    }

    @Override
    public CategoryEntity findById(final Long id) {
        return this.categoryRepository.findById(id).orElseThrow(
                () -> new IllegalStateException(errIdNotFoundMessage("category", id)));
    }

    @Override
    @Transactional
    public CategoryEntity save(final CategoryEntity entity) {
        this.categoryRepository.findByTitle(entity.getTitle()).ifPresent(categoryEntity -> {
            throw new IllegalStateException(
                    errAlreadyExistMessage("category", "category title", entity.getTitle()));
        });
        entity.setId(null);
        validateEntityFieldsAnnotations(entity, true);
        return this.categoryRepository.save(entity);
    }

    @Override
    public void delete(final Long id) {
        final CategoryEntity existingCategory = findById(id);
        final Optional<CategoryEntity> categoryEntity = this.findWithAssociatedDiscounts(id);
        if (categoryEntity.isPresent() && categoryEntity.get().getDiscounts().size() > 0)
            throw new IllegalStateException(errAssociatedEntity("category","discount"));
        this.categoryRepository.delete(existingCategory);
    }

    @Override
    @Transactional
    public Optional<CategoryEntity> findWithAssociatedDiscounts(final Long id) {
        return this.categoryRepository.findWithAssociatedDiscounts(id);
    }
}
