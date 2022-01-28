package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.Discount;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface DiscountService {

    Optional<Discount> findByIdDiscount(Long id);

    List<Optional<Discount>> findAllDiscounts();

    Optional<Discount> createDiscount(Discount discount);

    Optional<Discount> updateDiscountById(Long id, Discount discount);

    void deleteDiscountById(Long id);

}
