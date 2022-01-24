package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.Discount;

import java.util.List;
import java.util.Optional;

public interface DiscountService {


    Optional<Discount> findByIdDiscount(Long id);

    List<Discount> findAllDiscounts();

    Discount createDiscount(Discount discount);

    Discount updateDiscountById(Long id, Discount newDiscount);

    void deleteDiscountById(Long id);

    List<Discount> filterByTitle(String name);

}
