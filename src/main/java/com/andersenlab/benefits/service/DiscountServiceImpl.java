package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.Discount;
import com.andersenlab.benefits.repository.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
public class DiscountServiceImpl implements DiscountService {


    private final DiscountRepository discountRepository;


    @Autowired
    public DiscountServiceImpl(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @Override
    public final Optional<Discount> findByIdDiscount(final Long id) {
        return discountRepository.findById(id);
    }

    @Override
    public final List<Optional<Discount>> findAllDiscounts() {
        return discountRepository.findAll().stream()
                .map(discount -> Optional.of(Objects.requireNonNullElseGet(discount, Discount::new)))
                .toList();
    }

    @Override
    public final Optional<Discount> createDiscount(final Discount discount) {
        return Optional.of(discountRepository.save(discount));
    }

    @Override
    public final Optional<Discount> updateDiscountById(final Long id, final Discount newDiscount) {
        discountRepository.findById(id).map(discount -> {
            discount.setTitle(newDiscount.getTitle());
            discount.setDescription(newDiscount.getDescription());
            discount.setDateBegin(newDiscount.getDateBegin());
            discount.setDateFinish(newDiscount.getDateFinish());
            discount.setSizeDiscount(newDiscount.getSizeDiscount());
            discount.setImageDiscount(newDiscount.getImageDiscount());
            discount.setArea(newDiscount.getArea());
            return discountRepository.save(discount);
        }).orElseThrow(() -> {
            return new RuntimeException("The problem with updates discount");
        });

        return Optional.of(newDiscount);
    }

    @Override
    public void deleteDiscountById(final Long id) {
        discountRepository.deleteById(id);
    }

}
