package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.service.RoleService;
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

@Tag(name="Role controller", description="Controller for performing operations on user roles.")
@RestController
public class RoleController {
	private final RoleService roleService;
	
	@Autowired
	public RoleController(final RoleService roleService) {
		this.roleService = roleService;
	}
	
	@Operation(summary = "This is to fetch all the roles stored in DB")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Details of all the roles",
					content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "500",
					description = "Internal Server Error",
					content = @Content)
	})
	@GetMapping("/roles")
	public List<RoleEntity> getRoles() {
		return roleService.findAll();
	}
	
	@Operation(summary = "This is to update the role")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Role has been updated",
					content = @Content),
			@ApiResponse(responseCode = "500",
					description = "Internal Server Error",
					content = @Content)
	})
	@PutMapping("/roles")
	public void updateRole(@RequestBody final RoleEntity roleEntity) {
		roleService.findById(roleEntity.getId())
				.orElseThrow(() -> new IllegalStateException("Role with this id was not found in the database"));
		
		final Optional<RoleEntity> roleEntityWithExistCode = roleService.findByCode(roleEntity.getCode());
		
		if (roleEntityWithExistCode.isEmpty() ||
				roleEntity.getId().equals(
						roleEntityWithExistCode.get().getId())) {
			roleService.updateRoleEntity(roleEntity.getId(), roleEntity.getName(), roleEntity.getCode());
			
		} else {
			throw new IllegalStateException("Role with such 'code' is already exists");
		}
	}
	
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
	public ResponseEntity<?> addRole(
			@RequestParam(value = "name") final String name,
			@RequestParam(value = "code") final String code) {
		
		roleService.findByCode(code)
				.ifPresent(roleEntity -> {
					throw new IllegalStateException("Role with such 'code' is already exists");});
		
		final RoleEntity roleEntityToWrite = new RoleEntity(name, code);
		final RoleEntity savedRoleEntity = roleService.save(roleEntityToWrite);
		
		return new ResponseEntity<>(savedRoleEntity, HttpStatus.CREATED);
	}
	
	@Operation(summary = "This is to get the role")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Role has been received",
					content = @Content),
			@ApiResponse(responseCode = "500",
					description = "Internal Server Error",
					content = @Content
			)
	})
	@GetMapping("/roles/{id}")
	public RoleEntity getRole(@PathVariable @DecimalMin("1") Long id) {
		final Optional<RoleEntity> roleEntity = roleService.findById(id);
		
		return roleEntity.orElseThrow(
				() -> new IllegalStateException("Role with this id was not found in the database"));
	}
	
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
	public void deleteRole(@PathVariable @DecimalMin("1") Long id) {
		roleService.findById(id)
				.orElseThrow(
						() -> new IllegalStateException("Role with this id was not found in the database"));
		
		roleService.delete(id);
	}
}
