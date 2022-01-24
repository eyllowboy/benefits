package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.Discount;
import com.andersenlab.benefits.repository.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class DiscountServiceImpl implements DiscountService{


    private final DiscountRepository discountRepository;


    @Autowired
    public DiscountServiceImpl(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @Override
    public Optional<Discount> findByIdDiscount(Long id) {
        return discountRepository.findById(id);
    }

    @Override
    public List<Discount> findAllDiscounts() {
        return discountRepository.findAll();
    }

    @Override
    public Discount createDiscount(Discount discount) throws IllegalArgumentException {
        if(discount == null)
            throw new IllegalArgumentException("Discount not found");

        return discountRepository.save(discount);

    }

    @Override
    public Discount updateDiscountById(Long id, Discount newDiscount) {
        discountRepository.findById(id)
                .map(discount -> {
                    discount.setTitle(newDiscount.getTitle());
                    discount.setDescription(newDiscount.getDescription());
                    discount.setDateBegin(newDiscount.getDateBegin());
                    discount.setDateFinish(newDiscount.getDateFinish());
                    discount.setSizeDiscount(newDiscount.getSizeDiscount());
                    discount.setImageDiscount(newDiscount.getImageDiscount());
                    discount.setArea(newDiscount.getArea());
                    return discountRepository.save(discount);
                })
                .orElseGet(() -> {
                    newDiscount.setId(id);
                    return discountRepository.save(newDiscount);
                });

        return newDiscount;
    }

    public void deleteDiscountById(Long id) {
        discountRepository.deleteById(id);
    }

    public List<Discount> filterByTitle(String name) {
        return discountRepository.findByTitle(name);
    }


}
