package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.CategoryEntity;
import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.service.RoleService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.DecimalMin;

/**
 * A controller for handling requests for {@link RoleEntity}.
 *
 * @author Andrei Rabchun
 * @version 1.0
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
@Tag(name = "Role controller", description = "Controller for performing operations on user roles.")
@RestController
@SecurityRequirement(name = "benefits")
public class RoleController {
    private final RoleService roleService;

    @Autowired
    public RoleController(final RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * @param page is number of page to start returned result from
     * @param size is number of elements per page that needs to return
     * @param sort is the field by which to sort elements in returned page
     * @return a page of {@link RoleEntity} from database.
     */
    @Operation(summary = "This is to fetch all the roles stored in DB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Details of all the roles",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @GetMapping("/roles")
    public Page<RoleEntity> getRoles(@RequestParam(required = false, defaultValue = "0") final int page,
                                     @RequestParam(required = false, defaultValue = "6") final int size,
                                     @RequestParam(required = false, defaultValue = "id") final String sort) {
        return this.roleService.findAll(PageRequest.of(page, size, Sort.by(sort)));
    }

    /**
     * Updates {@link RoleEntity} in the database.
     *
     * @param id         is the id of {@link RoleEntity} that needs to update
     * @param roleEntity the {@link RoleEntity} that needs to update
     * @throws IllegalStateException if:
     *                               <ul>
     *                               <li>{@link RoleEntity} with given id was not found in the database
     *                               <li>{@link RoleEntity} with {@link RoleEntity#getCode()} field is already exists
     *                               </ul>
     */
    @Operation(summary = "This is to update the role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Role has been updated",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @PatchMapping("/roles/{id}")
    public ResponseEntity<RoleEntity> updateRole(@PathVariable final Long id,
                                                 @RequestBody final RoleEntity roleEntity) {
        return ResponseEntity.ok(this.roleService.update(id, roleEntity));
    }

    /**
     * Create {@link RoleEntity} in the database.
     *
     * @param role new {@link RoleEntity} to be added
     * @return post {@link RoleEntity}
     * @throws IllegalStateException if:<ul>
     *                               <li>{@link RoleEntity} with given id was not found in the database
     *                               <li>{@link RoleEntity} with {@link RoleEntity#getCode()} field is already exists
     *                               </ul>
     */
    @Operation(summary = "This is to create new role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Role has been created",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @PostMapping("/roles")
    public ResponseEntity<RoleEntity> addRole(@RequestBody final RoleEntity role) {
        return new ResponseEntity<>(this.roleService.save(role), HttpStatus.CREATED);
    }

    /**
     * Gets {@link RoleEntity} from the database.
     *
     * @param id the id of {@link RoleEntity} that needs to get
     * @return {@link RoleEntity}
     * @throws IllegalStateException if the given id was not found in the database
     */
    @Operation(summary = "This is to get the role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Role has been received",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @GetMapping("/roles/{id}")
    public RoleEntity getRole(@PathVariable @DecimalMin("1") final Long id) {
        return this.roleService.findById(id);
    }

    /**
     * Deletes {@link RoleEntity} from the database.
     *
     * @param id the id of {@link RoleEntity} that needs to delete
     * @throws IllegalStateException if the given id was not found in the database
     */
    @Operation(summary = "This is to remove the role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Role has been removed",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @DeleteMapping("/roles/{id}")
    public void deleteRole(@PathVariable @DecimalMin("1") final Long id) {
        this.roleService.delete(id);
    }
}
