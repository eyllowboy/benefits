package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.service.LocationService;
import com.andersenlab.benefits.service.RoleService;
import com.andersenlab.benefits.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.DecimalMin;
import java.util.Objects;
import java.util.Optional;

/**
 * A controller for handling requests for {@link UserEntity}.
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
@Tag(name = "User controller", description = "Controller for performing operations on users.")
@RestController
@SecurityRequirement(name = "benefits")
public class UserController {
    private final UserService userService;
    private final RoleService roleService;
    private final LocationService locationService;

    @Autowired
    public UserController(final UserService userService, final RoleService roleService,
                          final LocationService locationService) {
        this.userService = userService;
        this.roleService = roleService;
        this.locationService = locationService;
    }

    /**
     * @return a list of {@link UserEntity} from database.
     */
    @Operation(summary = "This is to fetch all the users stored in DB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Details of all the users",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @GetMapping("/users")
    public Page<UserEntity> getUsers(@RequestParam(required = false, defaultValue = "0") final int page,
                                     @RequestParam(required = false, defaultValue = "6") final int size,
                                     @RequestParam(required = false, defaultValue = "id") final String sort) {
        return this.userService.findAll(PageRequest.of(page, size, Sort.by(sort)));
    }

    /**
     * Updates {@link UserEntity} in the database.
     *
     * @param userEntity the {@link UserEntity} that needs to update
     * @throws IllegalStateException if:
     *                               <ul>
     *                               <li>{@link UserEntity} with given id was not found in the database
     *                               <li>{@link RoleEntity} with given id was not found in the database
     *                               <li>{@link LocationEntity} with given id was not found in the database
     *                               <li>{@link UserEntity} with {@link UserEntity#getLogin()} field is already exists
     *                               </ul>
     */
    @Operation(summary = "This is to update the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "User has been updated",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @PatchMapping("/users/{id}")
    public ResponseEntity<UserEntity> updateUser(@PathVariable final Long id,
                                                 @RequestBody final UserEntity userEntity) {

        if (!Objects.isNull(userEntity.getRoleEntity()))
            this.roleService.findById(userEntity.getRoleEntity().getId()).orElseThrow(() ->
                new IllegalStateException("Role with this id was not found in the database"));
        if (!Objects.isNull(userEntity.getLocation()))
            this.locationService.findById(userEntity.getLocation().getId()).orElseThrow(() ->
                    new IllegalStateException("Location with this id was not found in the database"));
        if (!Objects.isNull(userEntity.getLogin())) {
            final Optional<UserEntity> theSameUser = this.userService.findByLogin(userEntity.getLogin());
            if (theSameUser.isPresent() && !theSameUser.get().getId().equals(id))
                throw new IllegalStateException("User with such 'login' is already exists");
        }
        final UserEntity existingUser = this.userService.findById(userEntity.getId()).orElseThrow(() ->
                new IllegalStateException("User with this id was not found in the database"));
        BeanUtils.copyProperties(userEntity, existingUser, "id", "login");
        this.userService.updateUserEntity(id,
                    existingUser.getLogin(),
                    existingUser.getRoleEntity(),
                    existingUser.getLocation());
        return ResponseEntity.ok(existingUser);
    }

    /**
     * Create {@link UserEntity} in the database.
     *
     * @param login of new {@link UserEntity}
     * @param password of user just for authorization
     * @throws IllegalStateException if:
     *                               <ul>
     *                               <li>{@link UserEntity} with {@link UserEntity#getLogin()} field is already exists
     *                               <li>{@link RoleEntity} with given id was not found in the database
     *                               <li>{@link LocationEntity} with given id was not found in the database
     *                               </ul>
     */
    @Operation(summary = "This is to create new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "User has been created",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @PostMapping("/users")
    @Transactional
    public ResponseEntity<UserEntity> addUser(@RequestParam final String login,
                                              @RequestParam final String password) {
        return new ResponseEntity<>(this.userService.createNewUser(login, password), HttpStatus.CREATED);
    }

    /**
     * Gets {@link UserEntity} from the database.
     *
     * @param id the id of {@link UserEntity} that needs to get
     * @throws IllegalStateException if the given id was not found in the database
     */
    @Operation(summary = "This is to get the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "User has been received",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @GetMapping("/users/{id}")
    public UserEntity getUser(@PathVariable @DecimalMin("1") final Long id) {
        final Optional<UserEntity> userEntity = this.userService.findById(id);

        return userEntity.orElseThrow(
                () -> new IllegalStateException("User with this id was not found in the database"));
    }

    /**
     * Deletes {@link UserEntity} from the database.
     *
     * @param id the id of {@link UserEntity} that needs to delete
     * @throws IllegalStateException if the given id was not found in the database
     */
    @Operation(summary = "This is to remove the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "User has been removed",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable @DecimalMin("1") final Long id) {
        this.userService.findById(id)
                .orElseThrow(
                        () -> new IllegalStateException("User with this id was not found in the database"));

        this.userService.delete(id);
    }
}
