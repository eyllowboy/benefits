package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.CategoryEntity;
import com.andersenlab.benefits.domain.DiscountEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Interface for performing operations on a {@link CategoryEntity} database.
 * @author Denis Popov
 * @version 1.0
 * @see CrudService
 * @see CategoryEntity
 */

@Service
public interface CategoryService extends CrudService<CategoryEntity>{

    /***
     * Method to find {@link CategoryEntity} with specified title
     * @param title of category, not null
     * @return {@link CategoryEntity} with specified title, error if nothing found
     */
    Optional<CategoryEntity> findByTitle(final String title);

    /**
     * @param id the id of {@link CategoryEntity} in the database, not null
     * @param title the title of {@link CategoryEntity} stored in the database, not null
     */
    CategoryEntity updateCategoryEntity(final Long id, final String title);

    /**
     * Method to get {@link CategoryEntity} with EAGER fetch associated {@link DiscountEntity}
     * @param id the id of {@link CategoryEntity} need to load, not null
     * @return {@link CategoryEntity} with given id, error if id role found
     */
    Optional<CategoryEntity> findWithAssociatedDiscounts(final Long id);
}
