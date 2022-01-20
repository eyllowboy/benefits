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

@RestController
public class UserController {
	private RoleService roleService;
	private UserService userService;
	
	@Autowired
	public UserController(RoleService roleService, UserService userService) {
		this.roleService = roleService;
		this.userService = userService;
	}
	
	@GetMapping("/users")
	public List<UserEntity> getUsers() {
		return userService.findAll();
	}
	
	@GetMapping("/users/{id}")
	public UserEntity getUser(@PathVariable @DecimalMin("1") Integer id) {
		return userService.findById(id);
	}

	@PostMapping("/users")
	public ResponseEntity<?> addUser(
			@RequestParam(value = "login", required = false)
			@Size(min = 3, max = 20, message = "Login length is between 3 and 20 characters.")
					String login,
			@RequestBody RoleEntity roleEntity) {

		UserEntity userEntityWithExistLogin = userService.findByLogin(login);

		if (userEntityWithExistLogin == null) {
			userEntityWithExistLogin = new UserEntity(login, roleEntity);

			return new ResponseEntity<>(userService.save(userEntityWithExistLogin), HttpStatus.CREATED);

		} else {
			throw new IllegalStateException("User with such 'login' is already exists");
		}
	}

	@PutMapping("/users")
	public ResponseEntity<?> updateUser(@RequestBody UserEntity userEntity) {
		userService.findById(userEntity.getId());
		
		UserEntity userEntityWithExistLogin = userService.findByLogin(userEntity.getLogin());
		
		if (userEntityWithExistLogin == null) {
			return new ResponseEntity<>(userService.save(userEntity), HttpStatus.OK);

		} else {
			throw new IllegalStateException("User with such 'login' is already exists");
		}
	}

	@DeleteMapping("/users/{id}")
	public void deleteUser(@PathVariable @DecimalMin("1") Integer id) {
		userService.deleteUser(id);
	}
}
