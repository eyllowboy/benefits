package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.CategoryEntity;
import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.service.CategoryService;
import com.andersenlab.benefits.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * The controller for handling requests for {@link CategoryEntity}.
 * @author Denis Popov
 * @version 1.0
 */
@Tag(name = "Category controller", description = "Controller for performing operations on categories.")
@RestController
public class CategoryController {
    private final CategoryService categoryService;

    private final DiscountService discountService;

    @Autowired
    public CategoryController(CategoryService categoryService, DiscountService discountService) {
        this.categoryService = categoryService;
        this.discountService = discountService;
    }

    /**
     * Create {@link CategoryEntity} in the database.
     *
     * @param title of the new category {@link CategoryEntity#getTitle()}
     * @throws IllegalStateException if {@link CategoryEntity} with these parameters already exists.
     */
    @Operation(summary = "This is to create new category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Category has been created",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @PostMapping("/categories")
    public final ResponseEntity<CategoryEntity> addCategory(
            @RequestParam(value = "title") final String title) {
        categoryService.findByTitle(title).ifPresent(categoryEntity -> {
            throw new IllegalStateException("Category with title '" + title + "' already exists");
        });
        final CategoryEntity savedCategoryEntity = categoryService.save(new CategoryEntity(title));
        return new ResponseEntity<>(savedCategoryEntity, HttpStatus.CREATED);
    }

    /**
     * Gets {@link CategoryEntity} from the database with specified id.
     * @param id is the id of {@link CategoryEntity} that needs to get
     * @throws IllegalStateException if the given id was not found in the database
     */
    @Operation(summary = "This is to get the category by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Category has been received",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content
            )
    })
    @GetMapping("/categories/{id}")
    public CategoryEntity getCategoryById(@PathVariable final Long id) {
        return (categoryService.findById(id)).orElseThrow(
                () -> new IllegalStateException("Category with this id was not found in the database"));
    }

    /**
     * Updates {@link CategoryEntity} in the database.
     *
     * @param category the {@link CategoryEntity} that needs to update
     * @throws IllegalStateException if the {@link CategoryEntity} with given id was not found in the database.
     */
    @Operation(summary = "This is to update the category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Category has been updated",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @PutMapping("/categories")
    public void updateCategory(@RequestBody final CategoryEntity category) {
        categoryService.findById(category.getId())
                .orElseThrow(() -> new IllegalStateException("Category with this id was not found in the database"));
        categoryService.updateCategoryEntity(category.getId(), category.getTitle());
    }

    /**
     * Deletes {@link CategoryEntity} from the database.
     *
     * @param id is the id of {@link CategoryEntity} that needs to delete
     * @throws IllegalStateException if the given id was not found in the database
     */
    @Operation(summary = "This is to remove the category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Category has been removed.",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @DeleteMapping("/categories/{id}")
    @Transactional
    public void deleteCategory(@PathVariable final Long id) {
        Optional<CategoryEntity> category = categoryService.findById(id);
        if (category.isPresent()) {
            for (Optional<DiscountEntity> discount : discountService.findAllDiscounts())
                discount.ifPresent(discountEntity -> discountEntity.getCategories().remove(category.get()));
        } else {
            throw new IllegalStateException("Category with id: '"+ id +"' was not found in the database");
        };
//        categoryService.findById(id).orElseThrow(() -> new IllegalStateException("Category with id: '"+ id +"' was not found in the database"));
        categoryService.delete(id);
    }

    /**
     * Get list of all {@link CategoryEntity} from database.
     * @return a list of {@link CategoryEntity} from database.
     */
    @Operation(summary = "This is to fetch all the stored categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Details of all categories",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @GetMapping(value = "/categories")
    public List<CategoryEntity> getCategories() {
        return categoryService.findAll();
    }
}
