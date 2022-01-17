package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.Discount;
import com.andersenlab.benefits.exception.DiscountNotFoundException;
import com.andersenlab.benefits.repository.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class DiscountService {


    private final DiscountRepository discountRepository;


    @Autowired
    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }


     public Optional<Discount> findByIdDiscount(Integer id)  {
        return discountRepository.findById(id);
     }

     public List<Discount> findAllDiscounts(){
        return discountRepository.findAll();
     }

     public Discount createDiscount(Discount discount){
        return discountRepository.save(discount);

     }

     public Discount updateDiscountById(Integer id){
        Discount newDiscount = new Discount();
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
                .orElseGet(()->{
                    newDiscount.setId(id);
                    return discountRepository.save(newDiscount);
                });

         return newDiscount;
     }

     public void deleteDiscountById(Integer id){
        discountRepository.deleteById(id);
     }

     public List<Discount> filterNameDiscount(String name){
       List<Discount> fullList = discountRepository.findAll();
       return fullList.stream()
               .filter(t -> t.getTitle().contains(name))
               .collect(Collectors.toList());
     }




}
