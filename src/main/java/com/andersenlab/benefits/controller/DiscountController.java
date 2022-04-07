package com.andersenlab.benefits.controller;


import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.repository.DiscountSpec;
import com.andersenlab.benefits.service.impl.DiscountServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.andersenlab.benefits.repository.DiscountSpec.getLastAdded;

/**
 * The controller for handling requests for {@link DiscountEntity}.
 *
 * @author Aleksei Sidorin
 * @version 1.0
 * @see DiscountEntity
 * @see DiscountServiceImpl
 */

@ApiResponses(value = {
        @ApiResponse(responseCode = "401",
                description = "Unauthorized",
                content = @Content),
        @ApiResponse(responseCode = "403",
                description = "Forbidden",
                content = @Content),
        @ApiResponse(responseCode = "500",
                description = "Internal Server Error",
                content = @Content)
})
@Tag(name = "Discount controller", description = "Controller for performing operations on the discount")
@RestController
@SecurityRequirement(name = "benefits")
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
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/discounts")
    public List<DiscountEntity> allDiscount() {
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
                    content = @Content)
    })
    @PostMapping("/discounts")
    public ResponseEntity<DiscountEntity> newDiscount(@RequestBody final DiscountEntity newDiscount) {
        discountService.findByIdDiscount(newDiscount.getId()).ifPresent(discount -> {
            throw new IllegalStateException("The discount with id: " +
                    newDiscount.getId() + " already saved in the database");});
        final DiscountEntity savedDiscount = discountService.createDiscount(newDiscount)
                .orElseThrow(() -> new IllegalStateException("The discount with id: " +
                        newDiscount.getId() + " was not saved in the database"));
        return new ResponseEntity<>(savedDiscount, HttpStatus.CREATED);
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
                    content = @Content)
    })
    @GetMapping("/discounts/{id}")
    public Optional<DiscountEntity> oneDiscount(@PathVariable final Long id) {
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
                    content = @Content)
    })
    @PutMapping("/discounts/{id}")
    public Optional<DiscountEntity> updateDiscount(@PathVariable final Long id, @RequestBody final DiscountEntity discount) {
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
                    content = @Content)
    })
    @DeleteMapping("/discounts/{id}")
    public void deleteDiscount(@PathVariable final Long id) {
        discountService.findByIdDiscount(id).orElseThrow(() -> new IllegalStateException("The discount with id: " + id + " was not found in the database"));
        discountService.deleteDiscountById(id);
    }

    /**
     * Filters {@link DiscountEntity} from the database by city and lastDate.
     *
     * @param city the city {@link LocationEntity} that needs to filtering
     */
    @Operation(summary = "This is method to filtering the discount by city and lastDate.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Discount is filtered",
                    content = @Content)
    })
    @GetMapping("/discounts/filter-by-city")
    public List<DiscountEntity> findLastByCity(@RequestParam(required = false) final String city) {
        Specification<DiscountEntity> spec = Specification.where(DiscountSpec.getByLocation(city).and(getLastAdded()));
        return discountService.getDiscountsByCriteria(spec);
    }


    /**
     * Filters {@link DiscountEntity} from the database by category and lastDate.
     *
     * @param category the city {@link LocationEntity} that needs to filtering
     */
    @Operation(summary = "This is method to filtering the discount by category and lastDate.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Discount is filtered",
                    content = @Content)
    })
    @GetMapping("/discounts/filter-by-category")
    public List<DiscountEntity> findLastByCategory(@RequestParam(required = false) final String category) {
        Specification<DiscountEntity> spec = Specification.where(DiscountSpec.getByCategory(category).and(getLastAdded()));
        return discountService.getDiscountsByCriteria(spec);
    }

    /**
     * Filters {@link DiscountEntity} from the database by type and lastDate.
     *
     * @param type the type {@link DiscountEntity} that needs to filtering
     */
    @Operation(summary = "This is method to filtering the discount by type and lastDate.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Discounts are filtered",
                    content = @Content)
    })
    @GetMapping("/discounts/filter-by-type")
    public List<DiscountEntity> findLastByType(@RequestParam(required = false) final String type) {
        Specification<DiscountEntity> spec = Specification.where(DiscountSpec.getByType(type).and(getLastAdded()));
        return discountService.getDiscountsByCriteria(spec);
    }

    /**
     * Filters {@link DiscountEntity} from the database by discountSize and lastDate.
     *
     * @param size the size {@link DiscountEntity} that needs to filtering
     */
    @Operation(summary = "This is method to filtering the discount by size and lastDate.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Discounts are filtered",
                    content = @Content)
    })
    @GetMapping("/discounts/filter-by-size")
    public List<DiscountEntity> findLastBySize(@RequestParam(required = false) final String size) {
        Specification<DiscountEntity> spec = Specification.where(DiscountSpec.getBySize(size).and(getLastAdded()));
        return discountService.getDiscountsByCriteria(spec);
    }
}
