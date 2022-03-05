package com.andersenlab.benefits.controller;


import com.andersenlab.benefits.domain.CategoryEntity;
import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.repository.DiscountSpec;
import com.andersenlab.benefits.service.DiscountServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * The controller for handling requests for {@link DiscountEntity}.
 *
 * @author Aleksei Sidorin
 * @version 1.0
 * @see DiscountEntity
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
     * Find all {@link DiscountEntity} from the database.
     *
     * @return a list of {@link DiscountEntity} from database.
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
    public final List<DiscountEntity> allDiscount() {
        return discountService.findAllDiscounts()
                .stream()
                .map(d -> d.orElseThrow(() -> new IllegalStateException("We have some problems with the database")))
                .toList();
    }

    /**
     * Create {@link DiscountEntity} in the database.
     *
     * @param newDiscount of the entity {@link DiscountEntity}
     * @throws IllegalStateException if {@link DiscountEntity} with this id was not saved in the database.
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
    public final ResponseEntity<DiscountEntity> newDiscount(@RequestBody final DiscountEntity newDiscount) {
        final Optional<DiscountEntity> savedDiscount = discountService.createDiscount(newDiscount);
        return new ResponseEntity<DiscountEntity>(
                savedDiscount.orElseThrow(() -> new IllegalStateException("The discount with id: " + newDiscount.getId() + " was not saved in the database")), HttpStatus.CREATED);
    }

    /**
     * Gets {@link DiscountEntity} from the database.
     *
     * @param id the id of {@link DiscountEntity} that needs to get.
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
    public final Optional<DiscountEntity> oneDiscount(@PathVariable final Long id) {
        final Optional<DiscountEntity> discount = discountService.findByIdDiscount(id);
        return Optional.ofNullable(discount.orElseThrow(() -> new IllegalStateException("The discount with id: " + id + " was not found in the database")));


    }

    /**
     * Updates {@link DiscountEntity} in the database.
     *
     * @param id       the id of {@link DiscountEntity} that needs to update
     * @param discount the {@link DiscountEntity} that needs to update
     * @throws IllegalStateException if the {@link DiscountEntity} with given id was not found in the database.
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
    public final Optional<DiscountEntity> updateDiscount(@PathVariable final Long id, @RequestBody final DiscountEntity discount) {
        discountService.findByIdDiscount(id).orElseThrow(() -> new IllegalStateException("The discount with id: " + id + " was not found in the database"));
        return discountService.updateDiscountById(id, discount);
    }

    /**
     * Deletes {@link DiscountEntity} from the database.
     *
     * @param id the id of {@link DiscountEntity} that needs to delete
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

    /**
     * Filters {@link DiscountEntity} from the database by city and category.
     *
     * @param city     the city {@link LocationEntity} that needs to filtering
     * @param category the category from {@link CategoryEntity} that needs to filtering
     */
    @Operation(summary = "This is method to filtering the discount by city and category.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Discount is filtered",
                    content = @Content)
    })
    @GetMapping("/discounts/filter")
    public final List<DiscountEntity> findByCityAndCategoryAndDate(@RequestParam(required = false) final String city, @RequestParam(required = false) final String category) {
        Specification<DiscountEntity> spec = Specification.where(DiscountSpec.getByLocation(city).and(DiscountSpec.getByCategory(category).and(DiscountSpec.getLastAdded())));
        return discountService.getDiscountsByCriteria(spec);
    }

    @Operation(summary = "This is method to filtering the discount by type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Discounts are filtered",
                    content = @Content)
    })
    /**
     * Filters {@link DiscountEntity} from the database by type.
     *
     * @param type the type {@link DiscountEntity} that needs to filtering
     */
    @GetMapping("/discounts/type")
    public final List<DiscountEntity> findByType(@RequestParam(required = false) final String type) {
        Specification<DiscountEntity> spec = Specification.where(DiscountSpec.getByType(type));
        return discountService.getDiscountsByCriteria(spec);
    }

    @Operation(summary = "This is method to filtering the discount by size.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Discounts are filtered",
                    content = @Content)
    })
    /**
     * Filters {@link DiscountEntity} from the database by discountSize.
     *
     * @param size the size {@link DiscountEntity} that needs to filtering
     */
    @GetMapping("/discounts/size")
    public final List<DiscountEntity> findBySizeDiscount(@RequestParam(required = false) final String size) {
        Specification<DiscountEntity> spec = Specification.where(DiscountSpec.getBySize(size));
        return discountService.getDiscountsByCriteria(spec);
    }

}

