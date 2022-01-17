package com.andersenlab.benefits.controller;


import com.andersenlab.benefits.domain.Discount;
import com.andersenlab.benefits.exception.DiscountNotFoundException;
import com.andersenlab.benefits.service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/discounts/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class DiscountController {

    private final DiscountService discountService;


    @Autowired
    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }


    @GetMapping("/")
    List<Discount> allDiscount(){
        return discountService.findAllDiscounts();
    }

    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    Optional<Discount> newDiscount(@RequestBody Discount newDiscount){
        return Optional.ofNullable(discountService.createDiscount(newDiscount));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    Discount one(@PathVariable Integer id){
        return discountService.findByIdDiscount(id)
                .orElseThrow(() -> new DiscountNotFoundException("Discount not found " + id));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    Discount updateDiscount(@RequestBody Discount discount, @PathVariable Integer id){
        if(id == null){
            throw new DiscountNotFoundException("Discount cant be found in method updateDiscount" + id);
        }

        return discountService.updateDiscountById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteDiscount(@PathVariable Integer id){
        if(id == null){
            throw new DiscountNotFoundException("Discount cant be found in method deleteDiscount" + id);
        }

        discountService.deleteDiscountById(id);
    }







}
