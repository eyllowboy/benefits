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
@RequestMapping(path = "/benefits/api/v1/", produces = MediaType.APPLICATION_JSON_VALUE)
public class DiscountController {

    private final DiscountService discountService;

    @Autowired
    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }


    @GetMapping("/discounts")
    List<Discount> allDiscount() {
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
    Optional<Discount> oneDiscount(@PathVariable Integer id) {
        if (id == null) {
            new ResponseEntity<Discount>(HttpStatus.NO_CONTENT);
        }
        return discountService.findByIdDiscount(id);

    }

    @PutMapping("/discount/{id}")
    @ResponseStatus(HttpStatus.OK)
    Discount updateDiscount(@RequestBody Discount discount, @PathVariable Integer id) {
        if (id == null) {
            new ResponseEntity<Discount>(HttpStatus.BAD_REQUEST);
        }

        return discountService.updateDiscountById(id);
    }

    @DeleteMapping("/discount/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteDiscount(@PathVariable Integer id) {
        if (id == null) {
            new ResponseEntity<Discount>(HttpStatus.NO_CONTENT);
        }

        discountService.deleteDiscountById(id);
    }

    @PostMapping("/discount/{title}")
    void filterDiscountByTitle(@PathVariable String title) {
        List<Discount> discounts = discountService.filterByTitle(title);

        if (discounts == null) {
            discountService.findAllDiscounts();
        }

    }


}
