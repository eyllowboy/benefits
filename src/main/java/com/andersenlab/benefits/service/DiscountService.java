package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.Discount;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface DiscountService {

    Optional<Discount> findByIdDiscount(final Long id);

    List<Optional<Discount>> findAllDiscounts();

    Optional<Discount> createDiscount(final Discount discount);

    Optional<Discount> updateDiscountById(final Long id, final Discount discount);

    void deleteDiscountById(final Long id);

}
