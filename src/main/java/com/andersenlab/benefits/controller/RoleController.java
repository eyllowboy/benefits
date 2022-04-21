package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.service.RoleService;
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
import javax.validation.constraints.DecimalMin;
import java.util.List;
import java.util.Optional;

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
     * @return a list of {@link RoleEntity} from database.
     */
    @Operation(summary = "This is to fetch all the roles stored in DB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Details of all the roles",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/roles")
    public Page<RoleEntity> getRoles(final Pageable pageable) {
        return this.roleService.findAll(pageable);
    }

    /**
     * Updates {@link RoleEntity} in the database.
     *
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
                    content = @Content)
    })
    @PatchMapping("/roles/{id}")
    public ResponseEntity<RoleEntity> updateRole(@PathVariable final Long id,
                                                 @RequestBody final RoleEntity roleEntity) {
        final RoleEntity existingRole = this.roleService.findById(id)
            .orElseThrow(() -> new IllegalStateException("Role with this id was not found in the database"));
        this.roleService.findByCode(roleEntity.getCode()).ifPresent(foundRole -> {
            throw new IllegalStateException("Role with such 'code' is already exists");});
        BeanUtils.copyProperties(roleEntity, existingRole, "id");
        this.roleService.updateRoleEntity(id, existingRole.getName(), existingRole.getCode());
        return ResponseEntity.ok(existingRole);
    }

    /**
     * Create {@link RoleEntity} in the database.
     *
     * @param role new {@link RoleEntity} to be added
     * @throws IllegalStateException if {@link RoleEntity} with {@link RoleEntity#getCode()} field is already exists
     */
    @Operation(summary = "This is to create new role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Role has been created",
                    content = @Content)
    })
    @PostMapping("/roles")
    public ResponseEntity<RoleEntity> addRole(@Valid @RequestBody final RoleEntity role) {
        this.roleService.findByCode(role.getCode()).ifPresent(roleEntity -> {
                    throw new IllegalStateException("Role with such 'code' is already exists");}
        );
        final RoleEntity savedRoleEntity = this.roleService.save(role);
        return new ResponseEntity<>(savedRoleEntity, HttpStatus.CREATED);
    }

    /**
     * Gets {@link RoleEntity} from the database.
     *
     * @param id the id of {@link RoleEntity} that needs to get
     * @throws IllegalStateException if the given id was not found in the database
     */
    @Operation(summary = "This is to get the role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Role has been received",
                    content = @Content)
    })
    @GetMapping("/roles/{id}")
    public RoleEntity getRole(@PathVariable @DecimalMin("1") final Long id) {
        final Optional<RoleEntity> roleEntity = this.roleService.findById(id);

        return roleEntity.orElseThrow(
                () -> new IllegalStateException("Role with this id was not found in the database"));
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
                    content = @Content)
    })
    @DeleteMapping("/roles/{id}")
    public void deleteRole(@PathVariable @DecimalMin("1") final Long id) {
        this.roleService.findById(id).orElseThrow(() ->
                new IllegalStateException("Role with this id was not found in the database"));
        final Optional<RoleEntity> roleEntity = this.roleService.findWithAssociatedUsers(id);
        if (roleEntity.isPresent() && roleEntity.get().getUsers().size() > 0)
                throw new IllegalStateException("There is active users with this Role in database");
        this.roleService.delete(id);
    }
}
