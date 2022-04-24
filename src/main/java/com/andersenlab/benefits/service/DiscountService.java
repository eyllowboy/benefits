package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.DiscountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Main interface for performing basic operations on database {@link DiscountEntity}.
 *
 * @author Aleksei Sidorin
 * @version 1.0
 */

@Service
public interface DiscountService {

    /**
     * Method allows us finds the discount by id.
     *
     * @param id the id of record in the database, not null
     * @return the discount corresponding given id from database, error if id not found
     */
    Optional<DiscountEntity> findByIdDiscount(final Long id);

    /**
     * Method allows us finds all discount.
     *
     * @return the list of discounts from database, error if not processed
     */
    Page<DiscountEntity> findAllDiscounts(final Pageable pageable);

    /**
     * Method allows creates new the discount.
     *
     * @param discount contains information to create a new record in the database, not null
     * @return the entity corresponding new record in the database, error if consistency conditions are not met
     */
    Optional<DiscountEntity> createDiscount(final DiscountEntity discount);

    /**
     * Method allows updates the discount by id.
     *
     * @param id       the id of record in the database, not null
     * @param discount contains information to the updated record in the database, not null
     * @return the entity corresponding updated record in the database, error if consistency conditions are not met
     */
    Optional<DiscountEntity> updateDiscountById(final Long id, final DiscountEntity discount);

    /**
     * Method allows deleted the discount by id.
     *
     * @param id the id of record in the database, not null
     */
    void deleteDiscountById(final Long id);

    /**
     * Method allows filtering the discount entity.
     *
     * @param specificDiscountEntity entity provide filtering data
     */
    Page<DiscountEntity> getDiscountsByCriteria(final Specification<DiscountEntity> specificDiscountEntity, final Pageable pageable);

    /**
     * Method allows search similar discounts.
     *
     * @param category string with category name in which to search
     * @param sizeDiscount string which must be contained in {@link DiscountEntity}'s size or vice versa
     * @param city is name of City where to search (optional) if certain location needed
     * @param limit number of {@link DiscountEntity} to return
     * @return List of {@link DiscountEntity} suitable to search conditions
     */
    List<DiscountEntity> getSimilarDiscounts(final String category,
                                             final String sizeDiscount,
                                             final String city,
                                             final Integer limit);
}

