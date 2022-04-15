package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.repository.DiscountRepository;
import com.andersenlab.benefits.service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The implementation for performing operations on a {@link DiscountEntity}.
 *
 * @author Aleksei Sidorin
 * @version 1.0
 * @see DiscountEntity
 * @see DiscountService
 */


@Service
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;


    @Autowired
    public DiscountServiceImpl(final DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @Override
    public Optional<DiscountEntity> findByIdDiscount(final Long id) {
        return discountRepository.findById(id);
    }

    @Override
    public Page<DiscountEntity> findAllDiscounts(final Pageable pageable) {
        return discountRepository.findAll(pageable);
    }

    @Override
    public Optional<DiscountEntity> createDiscount(final DiscountEntity discount) {
        return Optional.of(discountRepository.save(discount));
    }

    @Override
    public Optional<DiscountEntity> updateDiscountById(final Long id, final DiscountEntity newDiscount) {
        discountRepository.findById(id).map(discount -> {
            discount.setType(newDiscount.getType());
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

    @Override
    public List<DiscountEntity> getDiscountsByCriteria(final Specification<DiscountEntity> specificDiscountEntity) {
        return discountRepository.findAll(specificDiscountEntity);
    }
}
