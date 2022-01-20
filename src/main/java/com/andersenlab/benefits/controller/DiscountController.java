package com.andersenlab.benefits.controller;


import com.andersenlab.benefits.domain.Discount;
import com.andersenlab.benefits.service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class DiscountController {

    private final DiscountService discountService;

    @Autowired
    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }


    @GetMapping("/discounts")
    public List<Discount> allDiscount() {
        return discountService.findAllDiscounts();
    }

    @PostMapping("/discount/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    Optional<Discount> newDiscount(@RequestBody Discount newDiscount) {
        if (discountService.findByIdDiscount(newDiscount.getId()) != null) {
            new ResponseEntity<Discount>(HttpStatus.BAD_REQUEST);
        }
        return Optional.ofNullable(discountService.createDiscount(newDiscount));
    }

    @GetMapping("/discount/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Discount> oneDiscount(@PathVariable Integer id) {
        if (id == null) {
            new ResponseEntity<Discount>(HttpStatus.NO_CONTENT);
        }
        return discountService.findByIdDiscount(id);

    }

    @PutMapping("/discount/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Discount updateDiscount(@PathVariable Integer id, @RequestBody Discount discount) {
        if (id == null) {
            new ResponseEntity<Discount>(HttpStatus.BAD_REQUEST);
        }

        return discountService.updateDiscountById(id, discount);
    }

    @DeleteMapping("/discount/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDiscount(@PathVariable Integer id) {
        if (id == null) {
            new ResponseEntity<Discount>(HttpStatus.NO_CONTENT);
        }

        discountService.deleteDiscountById(id);
    }

    @GetMapping("/discount-{title}")
    @ResponseStatus(HttpStatus.OK)
    public List<Discount> filterDiscountByTitle(@PathVariable String title) {
        List<Discount> discounts = discountService.filterByTitle(title);

        if (discounts == null) {
            new ResponseEntity<Discount>(HttpStatus.NO_CONTENT);


        }
        return discounts;

    }




}
