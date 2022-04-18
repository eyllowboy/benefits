package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.CategoryEntity;
import com.andersenlab.benefits.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * The controller for handling requests for {@link CategoryEntity}.
 *
 * @author Denis Popov
 * @version 1.0
 */
@Tag(name = "Category controller", description = "Controller for performing operations on categories.")
@RestController
@SecurityRequirement(name = "benefits")
public class CategoryController {
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(final CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Create {@link CategoryEntity} in the database.
     *
     * @param category new {@link CategoryEntity} to be added
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
    public ResponseEntity<CategoryEntity> addCategory(@Valid @RequestBody final CategoryEntity category) {
        this.categoryService.findByTitle(category.getTitle()).ifPresent(categoryEntity -> {
            throw new IllegalStateException("Category with title '" + category.getTitle() + "' already exists");
        });
        final CategoryEntity savedCategoryEntity = this.categoryService.save(category);
        return new ResponseEntity<>(savedCategoryEntity, HttpStatus.CREATED);
    }

    /**
     * Gets {@link CategoryEntity} from the database with specified id.
     *
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
    @PatchMapping("/categories/{id}")
    public ResponseEntity<CategoryEntity> updateCategory(@PathVariable final Long id,
                                                         @RequestBody final CategoryEntity category) {
        CategoryEntity existingCategory = this.categoryService.findById(id)
                .orElseThrow(() -> new IllegalStateException("Category with this id was not found in the database"));
        BeanUtils.copyProperties(category, existingCategory, "id");
        this.categoryService.updateCategoryEntity(existingCategory.getId(), existingCategory.getTitle());
        return ResponseEntity.ok(existingCategory);
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
    public void deleteCategory(@PathVariable final Long id) {
        this.categoryService.findById(id).orElseThrow(() ->
                new IllegalStateException("Category with id: '" + id + "' was not found in the database"));
        final Optional<CategoryEntity> categoryEntity = this.categoryService.findWithAssociatedDiscounts(id);
        if (categoryEntity.isPresent() && categoryEntity.get().getDiscounts().size() > 0)
            throw new IllegalStateException("There is active discounts in this Category in database");
        this.categoryService.delete(id);
    }

    /**
     * Get list of all {@link CategoryEntity} from database.
     *
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
    public Page<CategoryEntity> getCategories(final Pageable pageable) {
        return categoryService.findAll(pageable);
    }
}
