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

@RestController
public class RoleController {
	private RoleService roleService;
	
	@Autowired
	public RoleController(RoleService roleService) {
		this.roleService = roleService;
	}
	
	@GetMapping("/roles")
	public List<RoleEntity> getRoles() {
		return roleService.findAll();
	}
	
	@GetMapping("/roles/{id}")
	public RoleEntity getRole(@PathVariable @DecimalMin("1") Integer id) {
		return roleService.findById(id);
	}
	
	@PostMapping("/roles")
	public ResponseEntity<?> addRole(
			@RequestParam(value = "name", required = false)
			@Size(min = 3, max = 25, message = "Name length is between 3 and 25 characters.")
					String name,
			@RequestParam(value = "code", required = false)
			@Size(min = 3, max = 20, message = "Code length is between 3 and 20 characters.")
					String code) {
		
		RoleEntity roleEntityWithExistCode = roleService.findByCode(code);
		
		if (roleEntityWithExistCode == null) {
			roleEntityWithExistCode = new RoleEntity(name, code);
			
			return new ResponseEntity<>(roleService.save(roleEntityWithExistCode), HttpStatus.CREATED);
			
		} else {
			throw new IllegalStateException("Role with such 'code' is already exists");
		}
	}
	
	@PutMapping("/roles")
	public ResponseEntity<?> updateRole(@RequestBody RoleEntity roleEntity) {
		roleService.findById(roleEntity.getId());
		
		RoleEntity roleEntityWithExistCode = roleService.findByCode(roleEntity.getCode());
		
		if (roleEntityWithExistCode == null) {
			return new ResponseEntity<>(roleService.save(roleEntity), HttpStatus.OK);
			
		} else {
			throw new IllegalStateException("Role with such 'code' is already exists");
		}
	}
	
	@DeleteMapping("/roles/{id}")
	public void deleteRole(@PathVariable @DecimalMin("1") Integer id) {
		roleService.deleteRole(id);
	}
}
