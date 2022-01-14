package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.Discount;
import com.andersenlab.benefits.service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DiscountController {

    private final DiscountService discountService;

    @Autowired
    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @GetMapping("/discount")
    public List<Discount> getDiscountAll(){
        List<Discount> discountList =  discountService.findAllDiscounts();
        return discountList;
    }

    @GetMapping("discount/{id}")
    public Discount getDiscount(@PathVariable Integer id){
        return discountService.findByIdDiscount(id);
    }

    @PostMapping("/discount/addNew")
    public void addDiscount(@RequestBody Discount discount) {
        discountService.createDiscount(discount);
    }

    @PutMapping("/discount/{id}/update")
    public void updateDiscount(@RequestParam Integer id){
        discountService.updateDiscountById(id);
    }

    @DeleteMapping("/discount/{id}/delete")
    public void deleteDiscount(@PathVariable Integer id){
        discountService.deleteDiscountById(id);
    }



}
