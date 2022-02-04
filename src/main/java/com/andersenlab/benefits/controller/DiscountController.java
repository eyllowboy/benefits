package com.andersenlab.benefits.controller;


import com.andersenlab.benefits.domain.Discount;
import com.andersenlab.benefits.service.DiscountServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * The controller for handling requests for {@link Discount}.
 *
 * @author Aleksei Sidorin
 * @version 1.0
 * @see Discount
 * @see DiscountServiceImpl
 */

@Tag(name = "Discount controller", description = "Controller for performing operations on the discount")
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class DiscountController {

    private final DiscountServiceImpl discountService;

    @Autowired
    public DiscountController(DiscountServiceImpl discountService) {
        this.discountService = discountService;
    }

    /**
     * Find all {@link Discount} from the database.
     *
     * @return a list of {@link Discount} from database.
     */
    @Operation(summary = "This is to fetch all discounts from database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Details of all the discounts",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @GetMapping("/discounts")
    public final List<Discount> allDiscount() {
        return discountService.findAllDiscounts()
                .stream()
                .map(d -> d.orElseThrow(() -> new IllegalStateException("We have some problems with the database")))
                .toList();
    }

    /**
     * Create {@link Discount} in the database.
     *
     * @throws IllegalStateException if {@link Discount} with this id was not saved in the database.
     */
    @Operation(summary = "This is create the new discount.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Discount has been created",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @PostMapping("/discounts")
    public final ResponseEntity<Discount> newDiscount(@RequestBody final Discount newDiscount) {
        final Optional<Discount> savedDiscount = discountService.createDiscount(newDiscount);
        return new ResponseEntity<Discount>(
                savedDiscount.orElseThrow(() -> new IllegalStateException("The discount with id: " + newDiscount.getId() + " was not saved in the database")), HttpStatus.CREATED);
    }

    /**
     * Gets {@link Discount} from the database.
     *
     * @param id the id of {@link Discount} that needs to get.
     * @throws IllegalStateException if the given id was not found in the database.
     */
    @Operation(summary = "This is to get the discount.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Discount has been received",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @GetMapping("/discounts/{id}")
    public final Optional<Discount> oneDiscount(@PathVariable final Long id) {
        final Optional<Discount> discount = discountService.findByIdDiscount(id);
        return Optional.ofNullable(discount.orElseThrow(() -> new IllegalStateException("The discount with id: " + id + " was not found in the database")));


    }

    /**
     * Updates {@link Discount} in the database.
     *
     * @param id the id of {@link Discount} that needs to update
     * @param discount the {@link Discount} that needs to update
     * @throws IllegalStateException if the {@link Discount} with given id was not found in the database.
     */
    @Operation(summary = "This is update the discount.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Discount has been updated",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @PutMapping("/discounts/{id}")
    public Optional<Discount> updateDiscount(@PathVariable final Long id, @RequestBody final Discount discount) {
        discountService.findByIdDiscount(id).orElseThrow(() -> new IllegalStateException("The discount with id: " + id + " was not found in the database"));
        return discountService.updateDiscountById(id, discount);
    }

    /**
     * Deletes {@link Discount} from the database.
     *
     * @param id the id of {@link Discount} that needs to delete
     * @throws IllegalStateException if the given id was not found in the database
     */
    @Operation(summary = "This is to remove the discount.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Discount has been deleted",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @DeleteMapping("/discounts/{id}")
    public void deleteDiscount(@PathVariable final Long id) {
        discountService.findByIdDiscount(id).orElseThrow(() -> new IllegalStateException("The discount with id: " + id + " was not found in the database"));
        discountService.deleteDiscountById(id);
    }

}

