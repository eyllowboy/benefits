package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.service.RoleService;
import com.andersenlab.benefits.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

@RestController
public class UserController {
	private final UserService userService;
	private final RoleService roleService;
	
	@Autowired
	public UserController(UserService userService, RoleService roleService) {
		this.userService = userService;
		this.roleService = roleService;
	}
	
	@GetMapping("/users")
	public List<UserEntity> getUsers() {
		return userService.findAll()
				.stream()
				.map(x -> x.orElseThrow(
				() -> new IllegalStateException("Having trouble getting data from the database")))
				.toList();
	}
	
	@PutMapping("/users")
	public void updateUser(@RequestBody UserEntity userEntity) {
		Optional<UserEntity> userEntityInDataBaseForUpdate = userService.findById(userEntity.getId());
		userEntityInDataBaseForUpdate
				.orElseThrow(() -> new IllegalStateException("User with this id was not found in the database"));
		
		Optional<RoleEntity> roleEntityInDataBase = roleService.findById(userEntity.getRoleEntity().getId());
		roleEntityInDataBase
				.orElseThrow(() -> new IllegalStateException("Role with this id was not found in the database"));
		
		Optional<UserEntity> userEntityWithSameLogin = userService.findByLogin(userEntity.getLogin());
		
		if (userEntityWithSameLogin.isPresent()
				&& !userEntityWithSameLogin.get().getId().equals(userEntity.getId())) {
			throw new IllegalStateException("User with such 'login' is already exists");
		} else {
			userService.updateUserEntity(userEntity.getId(),
					userEntity.getLogin(),
					userEntity.getRoleEntity());
		}
	}
	
	@PostMapping("/users")
	public ResponseEntity<?> addUser(
			@RequestParam(value = "login")
			@Size(min = 3, max = 20, message = "Login length is between 3 and 20 characters.")
					String login,
			@RequestParam(value = "roleId") Long roleId) {
		
		userService.findByLogin(login)
				.ifPresent(userEntity -> {throw new RuntimeException("User with such 'login' is already exists");});
		
		Optional<RoleEntity> roleEntityInDataBase = roleService.findById(roleId);
		roleEntityInDataBase
				.orElseThrow(() -> new IllegalStateException("Role with this id was not found in the database"));
		
		Optional<UserEntity> savedUserEntity = userService.save(new UserEntity(login, roleEntityInDataBase.get()));
		
		return new ResponseEntity<>(
				savedUserEntity.orElseThrow(
						() -> new IllegalStateException("Having trouble saving to the database"))
				, HttpStatus.CREATED);
	}
	
	@GetMapping("/users/{id}")
	public UserEntity getUser(@PathVariable @DecimalMin("1") Long id) {
		Optional<UserEntity> userEntity = userService.findById(id);
		
		return userEntity.orElseThrow(
				() -> new IllegalStateException("User with this id was not found in the database"));
	}
	
	@DeleteMapping("/users/{id}")
	public void deleteUser(@PathVariable @DecimalMin("1") Long id) {
		userService.findById(id)
				.orElseThrow(
						() -> new IllegalStateException("User with this id was not found in the database"));
		
		userService.delete(id);
	}
}
