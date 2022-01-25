package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

@RestController
public class RoleController {
	private final RoleService roleService;
	
	@Autowired
	public RoleController(RoleService roleService) {
		this.roleService = roleService;
	}
	
	@GetMapping("/roles")
	public List<RoleEntity> getRoles() {
		return roleService.findAll()
				.stream()
				.map(x -> x.orElseThrow(
						() -> new IllegalStateException("Having trouble getting data from the database")))
				.toList();
	}
	
	@PutMapping("/roles")
	public void updateRole(@RequestBody RoleEntity roleEntity) {
		roleService.findById(roleEntity.getId())
				.orElseThrow(() -> new IllegalStateException("Role with this id was not found in the database"));
		
		Optional<RoleEntity> roleEntityWithExistCode = roleService.findByCode(roleEntity.getCode());
		
		if (roleEntityWithExistCode.isEmpty()) {
			roleService.updateRoleEntity(roleEntity.getId(), roleEntity.getName(), roleEntity.getCode());
			
		} else {
			throw new IllegalStateException("Role with such 'code' is already exists");
		}
	}
	
	@PostMapping("/roles")
	public ResponseEntity<?> addRole(
			@RequestParam(value = "name")
			@Size(min = 3, max = 25, message = "Name length is between 3 and 25 characters.")
					String name,
			@RequestParam(value = "code")
			@Size(min = 3, max = 20, message = "Code length is between 3 and 20 characters.")
					String code) {
		
		roleService.findByCode(code)
				.ifPresent(roleEntity -> {throw new RuntimeException("Role with such 'code' is already exists");});
		
		RoleEntity roleEntityToWrite = new RoleEntity(name, code);
		Optional<RoleEntity> savedRoleEntity = roleService.save(roleEntityToWrite);
		
		return new ResponseEntity<>(
				savedRoleEntity.orElseThrow(
						() -> new IllegalStateException("Having trouble saving to the database"))
				, HttpStatus.CREATED);
	}
	
	@GetMapping("/roles/{id}")
	public RoleEntity getRole(@PathVariable @DecimalMin("1") Long id) {
		Optional<RoleEntity> roleEntity = roleService.findById(id);
		
		return roleEntity.orElseThrow(
				() -> new IllegalStateException("Role with this id was not found in the database"));
	}
	
	@DeleteMapping("/roles/{id}")
	public void deleteRole(@PathVariable @DecimalMin("1") Long id) {
		roleService.findById(id)
				.orElseThrow(
						() -> new IllegalStateException("Role with this id was not found in the database"));
		
		roleService.delete(id);
	}
}
