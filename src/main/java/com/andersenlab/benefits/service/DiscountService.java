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

    Optional<Discount> findByIdDiscount(final Long id);

    List<Optional<Discount>> findAllDiscounts();

    Optional<Discount> createDiscount(final Discount discount);

    Optional<Discount> updateDiscountById(final Long id, final Discount discount);

    void deleteDiscountById(final Long id);

}
