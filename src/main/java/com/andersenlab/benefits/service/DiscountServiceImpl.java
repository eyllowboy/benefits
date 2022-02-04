package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.Discount;
import com.andersenlab.benefits.repository.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The implementation for performing operations on a {@link Discount}.
 *
 * @author Aleksei Sidorin
 * @version 1.0
 * @see Discount
 * @see DiscountService
 */


@Service
public class DiscountServiceImpl implements DiscountService {


    private final DiscountRepository discountRepository;


    @Autowired
    public DiscountServiceImpl(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    /**
     * @param id  the id of record in the database, not null
     * @return the discount corresponding given id from database, error if id not found
     */
    @Override
    public final Optional<Discount> findByIdDiscount(final Long id) {
        return discountRepository.findById(id);
    }

    /**
     * @return the list of discounts from database, error if not processed
     */
    @Override
    public final List<Optional<Discount>> findAllDiscounts() {
        return discountRepository.findAll().stream()
                .map(discount -> Optional.of(Objects.requireNonNullElseGet(discount, Discount::new)))
                .toList();
    }

    /**
     * @param discount contains information to create a new record in the database, not null
     * @return the entity corresponding new record in the database, error if consistency conditions are not met
     */
    @Override
    public final Optional<Discount> createDiscount(final Discount discount) {
        return Optional.of(discountRepository.save(discount));
    }

    /**
     * @param id  the id of record in the database, not null
     * @param newDiscount contains information to the updated record in the database, not null
     * @return the entity corresponding updated record in the database, error if consistency conditions are not met
     */
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

    /**
     * @param id  the id of record in the database, not null
     */
    @Override
    public void deleteDiscountById(final Long id) {
        discountRepository.deleteById(id);
    }

}
