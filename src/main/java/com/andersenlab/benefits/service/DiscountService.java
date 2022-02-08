package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.Discount;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Main interface for performing basic operations on database {@link Discount}.
 *
 * @author Aleksei Sidorin
 * @version 1.0
 */

@Service
public interface DiscountService {

    /**
     * @param id  the id of record in the database, not null
     * @return the discount corresponding given id from database, error if id not found
     */
    Optional<Discount> findByIdDiscount(final Long id);

    /**
     * @return the list of discounts from database, error if not processed
     */
    List<Optional<Discount>> findAllDiscounts();

    /**
     * @param discount contains information to create a new record in the database, not null
     * @return the entity corresponding new record in the database, error if consistency conditions are not met
     */
    Optional<Discount> createDiscount(final Discount discount);

    /**
     * @param id  the id of record in the database, not null
     * @param discount contains information to the updated record in the database, not null
     * @return the entity corresponding updated record in the database, error if consistency conditions are not met
     */
    Optional<Discount> updateDiscountById(final Long id, final Discount discount);

    /**
     * @param id  the id of record in the database, not null
     */
    void deleteDiscountById(final Long id);

}
