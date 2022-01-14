package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.Discount;
import com.andersenlab.benefits.repository.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class DiscountService {


    private final DiscountRepository discountRepository;


    @Autowired
    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }


     public Discount findByIdDiscount(Integer id){
        return discountRepository.getById(id);
     }

     public List<Discount> findAllDiscounts(){
        return discountRepository.findAll();
     }

     public void createDiscount(Discount discount){
        discountRepository.save(discount);
     }

     public void updateDiscountById(Integer id){
        Optional<Discount> discount = discountRepository.findById(id);
        Discount discountNew = new Discount();
        discountNew.setTitle(discount.get().getTitle());
        discountNew.setSizeDiscount(discount.get().getSizeDiscount());
        discountNew.setDateBegin(discount.get().getDateBegin());
        discountNew.setDateFinish(discount.get().getDateFinish());
        discountNew.setArea(discount.get().getArea());
        discountNew.setImageDiscount(discount.get().getImageDiscount());
        discountRepository.save(discountNew);
     }

     public void deleteDiscountById(Integer id){
        discountRepository.deleteById(id);
     }


}
