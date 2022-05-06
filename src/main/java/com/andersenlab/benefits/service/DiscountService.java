package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.DiscountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Main interface for performing basic operations on database {@link DiscountEntity}.
 *
 * @author Aleksei Sidorin
 * @version 1.0
 */

@Service
public interface DiscountService extends CrudService<DiscountEntity> {
    /**
     * Method to find {@link DiscountEntity} by criteria
     *
     * @param specificDiscountEntity entity provide filtering data
     */
    Page<DiscountEntity> getDiscountsByCriteria(final Specification<DiscountEntity> specificDiscountEntity, final Pageable pageable);

    /**
     * Method to find similar discounts in certain category with similar size
     *
     * @param category string with category name in which to search
     * @param sizeDiscount integer which must be between {@link DiscountEntity#sizeMin} and {@link DiscountEntity#sizeMax}
     * @param city is name of City where to search (optional) if certain location needed
     * @param limit number of {@link DiscountEntity} to return
     * @return List of {@link DiscountEntity} suitable to search conditions
     */
    List<DiscountEntity> getSimilarDiscounts(final String category,
                                             final Integer sizeDiscount,
                                             final String city,
                                             final Integer limit);
}
