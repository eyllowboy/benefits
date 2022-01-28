package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.service.RoleService;
import com.andersenlab.benefits.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.DecimalMin;
import java.util.List;
import java.util.Optional;

@Tag(name="User controller", description="Controller for performing operations on users.")
@RestController
public class UserController {
	private final UserService userService;
	private final RoleService roleService;
	
	@Autowired
	public UserController(UserService userService, RoleService roleService) {
		this.userService = userService;
		this.roleService = roleService;
	}
	
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
	public List<UserEntity> getUsers() {
		return userService.findAll();
	}
	
	@Operation(summary = "This is to update the user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "User has been updated",
					content = @Content),
			@ApiResponse(responseCode = "500",
					description = "Internal Server Error",
					content = @Content)
	})
	@PutMapping("/users")
	public void updateUser(@RequestBody UserEntity userEntity) {
		Optional<UserEntity> userEntityInDataBaseForUpdate = userService.findById(userEntity.getId());
		userEntityInDataBaseForUpdate
				.orElseThrow(() -> new IllegalStateException("User with this id was not found in the database"));
		
		Optional<RoleEntity> roleEntityInDataBase = roleService.findById(userEntity.getRoleEntity().getId());
		roleEntityInDataBase
				.orElseThrow(() -> new IllegalStateException("User with this id was not found in the database"));
				
		Optional<UserEntity> userEntityWithSameLogin = userService.findByLogin(userEntity.getLogin());
		
		if (userEntityWithSameLogin.isPresent()
				&& !userEntityWithSameLogin.get().getId().equals(userEntity.getId())) {
			throw new IllegalStateException("User with such 'login' is already exists");
		} else {
			userService.updateUserEntity(userEntity.getId(),
					userEntity.getLogin(),
					roleEntityInDataBase.get());
		}
	}
	
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
	public ResponseEntity<?> addUser(
			@RequestParam(value = "login")
					String login,
			@RequestParam(value = "roleId") Long roleId) {
		
		userService.findByLogin(login)
				.ifPresent(userEntity -> {
					throw new IllegalStateException("User with such 'login' is already exists");});
		
		Optional<RoleEntity> roleEntityInDataBase = roleService.findById(roleId);
		roleEntityInDataBase
				.orElseThrow(() -> new IllegalStateException("User with this id was not found in the database"));
		
		UserEntity savedUserEntity = userService.save(new UserEntity(login, roleEntityInDataBase.get()));
		
		return new ResponseEntity<>(savedUserEntity, HttpStatus.CREATED);
	}
	
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
	public UserEntity getUser(@PathVariable @DecimalMin("1") Long id) {
		Optional<UserEntity> userEntity = userService.findById(id);
		
		return userEntity.orElseThrow(
				() -> new IllegalStateException("User with this id was not found in the database"));
	}
	
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
	public void deleteUser(@PathVariable @DecimalMin("1") Long id) {
		userService.findById(id)
				.orElseThrow(
						() -> new IllegalStateException("User with this id was not found in the database"));
		
		userService.delete(id);
	}
}
