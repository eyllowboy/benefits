package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.repository.DiscountSpec;
import com.andersenlab.benefits.service.impl.DiscountServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public DiscountController(final DiscountServiceImpl discountService) {
        this.discountService = discountService;
    }

    /**
     * Create {@link DiscountEntity} in the database.
     *
     * @param discount new {@link DiscountEntity} to be added
     * @return created {@link DiscountEntity}
     * @throws IllegalStateException if:
     *                               <ul>
     *                               <li>company from {@link DiscountEntity#getCompany()} was not found in the database
     *                               <li>some location from {@link DiscountEntity#getArea()} was not found in the database
     *                               <li>some category from {@link DiscountEntity#getCategories()} was not found in the database
     *                               </ul>
     */
    @Operation(summary = "This is create the new discount.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Discount has been created",
                    content = @Content)
    })
    @PostMapping("/discounts")
    public ResponseEntity<DiscountEntity> addDiscount(@RequestBody final DiscountEntity discount) {
        return new ResponseEntity<>(this.discountService.save(discount), HttpStatus.CREATED);
    }

    /**
     * Gets {@link DiscountEntity} from the database with specified id.
     *
     * @param id of {@link DiscountEntity} that needs to get
     * @return found {@link DiscountEntity}
     * @throws IllegalStateException if the given id was not found in the database
     */
    @Operation(summary = "This is to get the discount by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Discount has been received",
                    content = @Content)
    })
    @GetMapping("/discounts/{id}")
    public DiscountEntity getDiscountById(@PathVariable final Long id) {
        return this.discountService.findById(id);
    }

    /**
     * Gets all {@link DiscountEntity} from the database
     *
     * @param page is number of page to start returned result from
     * @param size is number of elements per page that needs to return
     * @param sort is the field by which to sort elements in returned list
     * @return a list of {@link DiscountEntity} from database.
     */
    @Operation(summary = "This is to get all discounts from database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Details of all discounts",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/discounts")
    public Page<DiscountEntity> getDiscounts(@RequestParam(required = false, defaultValue = "0") final int page,
                                             @RequestParam(required = false, defaultValue = "6") final int size,
                                             @RequestParam(required = false, defaultValue = "dateBegin") final String sort) {
        return this.discountService.findAll(PageRequest.of(page, size, Sort.by(sort)));
    }

    /**
     * Find {@link DiscountEntity} with {@link DiscountEntity#area} city like "%city%"
     * in descending order by {@link DiscountEntity#dateBegin}
     *
     * @param city is partial mask of city name which mast be contained in
     *             {@link DiscountEntity#area} city. Default - all
     * @param page is number of page to start returned result from
     * @param size is number of elements per page that needs to return
     * @param sort is the field by which to sort elements in returned list
     * @return a list of found {@link DiscountEntity}
     */
    @Operation(summary = "This is method to get discounts by %city% mask ordered by descending beginDate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Discounts have been received",
                    content = @Content)
    })

    @GetMapping("/discounts/find-by-city")
    public Page<DiscountEntity> findDiscountByCity(@RequestParam(required = false) final String city,
                                                   @RequestParam(required = false, defaultValue = "0") final int page,
                                                   @RequestParam(required = false, defaultValue = "6") final int size,
                                                   @RequestParam(required = false, defaultValue = "dateBegin") final String sort) {
        final Specification<DiscountEntity> spec = Specification.where(DiscountSpec.getByLocation(city).and(getLastAdded()));
        return this.discountService.getDiscountsByCriteria(spec, PageRequest.of(page, size, Sort.by(sort)));
    }

    /**
     * Gets {@link DiscountEntity} with {@link DiscountEntity#categories} title equals to "category"
     * in descending order by {@link DiscountEntity#dateBegin}
     *
     * @param category is name of category which mast be equal to one of the
     *                 {@link DiscountEntity#categories} title. Default - all
     * @param page     is number of page to start returned result from
     * @param size     is number of elements per page that needs to return
     * @param sort     is the field by which to sort elements in returned list
     */
    @Operation(summary = "This is method to get discounts within certain category and ordered by descending beginDate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Discounts have been received",
                    content = @Content)
    })
    @GetMapping("/discounts/find-by-category")
    public Page<DiscountEntity> findDiscountByCategory(@RequestParam(required = false) final String category,
                                                       @RequestParam(required = false, defaultValue = "0") final int page,
                                                       @RequestParam(required = false, defaultValue = "6") final int size,
                                                       @RequestParam(required = false, defaultValue = "dateBegin") final String sort) {
        final Specification<DiscountEntity> spec = Specification.where(DiscountSpec.getByCategory(category).and(getLastAdded()));
        return this.discountService.getDiscountsByCriteria(spec, PageRequest.of(page, size, Sort.by(sort)));
    }

    /**
     * Find {@link DiscountEntity} with {@link DiscountEntity#type} like "%type%"
     * in descending order by {@link DiscountEntity#dateBegin}
     *
     * @param type is partial mask of "Type of company or service" which mast be contained in
     *             {@link DiscountEntity#type}. Default - all
     * @param page is number of page to start returned result from
     * @param size is number of elements per page that needs to return
     * @param sort is the field by which to sort elements in returned list
     */
    @Operation(summary = "This is method to find discounts by %type% mask ordered by descending beginDate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Discounts have been received",
                    content = @Content)
    })
    @GetMapping("/discounts/find-by-type")
    public Page<DiscountEntity> findDiscountByType(@RequestParam(required = false) final String type,
                                                   @RequestParam(required = false, defaultValue = "0") final int page,
                                                   @RequestParam(required = false, defaultValue = "6") final int size,
                                                   @RequestParam(required = false, defaultValue = "dateBegin") final String sort) {
        final Specification<DiscountEntity> spec = Specification.where(DiscountSpec.getByType(type).and(getLastAdded()));
        return this.discountService.getDiscountsByCriteria(spec, PageRequest.of(page, size, Sort.by(sort)));
    }

    /**
     * Find {@link DiscountEntity} with {@link DiscountEntity#sizeDiscount} like "%sizeDiscount%"
     * in descending order by {@link DiscountEntity#dateBegin}
     *
     * @param sizeDiscount is partial mask which mast be contained in
     *                     {@link DiscountEntity#sizeDiscount}. Default - all
     * @param page         is number of page to start returned result from
     * @param size         is number of elements per page that needs to return
     * @param sort         is the field by which to sort elements in returned list
     */
    @Operation(summary = "This is method to find discounts by %sizeDiscount% mask ordered by descending beginDate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Discounts have been received",
                    content = @Content)
    })
    @GetMapping("/discounts/find-by-size")
    public Page<DiscountEntity> findDiscountBySize(@RequestParam(required = false) final String sizeDiscount,
                                                   @RequestParam(required = false, defaultValue = "0") final int page,
                                                   @RequestParam(required = false, defaultValue = "6") final int size,
                                                   @RequestParam(required = false, defaultValue = "dateBegin") final String sort) {
        final Specification<DiscountEntity> spec = Specification.where(DiscountSpec.getBySize(sizeDiscount).and(getLastAdded()));
        return this.discountService.getDiscountsByCriteria(spec, PageRequest.of(page, size, Sort.by(sort)));
    }

    /**
     * Find {@link DiscountEntity} based on equals to category and like %sizeDiscount%
     *
     * @param category     is name of category which mast be equal to one of the
     *                     {@link DiscountEntity#categories} title.
     * @param sizeDiscount is string which must be contained in {@link DiscountEntity#sizeDiscount}
     *                     or vice versa
     * @param city         is name of City where to search (optional) if certain location needed
     * @param limit        is number of {@link DiscountEntity} to return
     * @return List of {@link DiscountEntity} suitable to search conditions
     */
    @Operation(summary = "This is method to find discounts in the same Category and with similar size of discount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Discounts have been received",
                    content = @Content)
    })
    @GetMapping("/discounts/find-similar")
    public List<DiscountEntity> findSimilar(@RequestParam final String category,
                                            @RequestParam final String sizeDiscount,
                                            @RequestParam(required = false) final String city,
                                            @RequestParam(required = false, defaultValue = "3") final Integer limit) {
        return this.discountService.getSimilarDiscounts(category, sizeDiscount, city, limit);
    }

    /**
     * Updates {@link DiscountEntity} in the database.
     *
     * @param id       of {@link DiscountEntity} to be updated
     * @param discount is {@link DiscountEntity} in which fields with not-null values need to be updated
     * @return updated {@link DiscountEntity}
     * @throws IllegalStateException if the {@link DiscountEntity} with given id was not found in the database.
     */
    @Operation(summary = "This is to update the discount.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Discount has been updated",
                    content = @Content)
    })
    @PatchMapping("/discounts/{id}")
    public ResponseEntity<DiscountEntity> updateDiscount(@PathVariable final Long id,
                                                         @RequestBody final DiscountEntity discount) {
        return ResponseEntity.ok(this.discountService.update(id, discount));
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
        this.discountService.delete(id);
    }

}
