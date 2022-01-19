package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class RoleServiceTest {
	@Autowired
	private RoleService roleService;
	
	@MockBean
	private RoleRepository roleRepository;
	
	@Test
	void findAll() {
		List<RoleEntity> roleEntities = List.of(
				new RoleEntity("user", "user"),
				new RoleEntity("user1", "user1"),
				new RoleEntity("user2", "user2"));
		
		when(roleRepository.findAll()).thenReturn(roleEntities);
		List<RoleEntity> foundRoleEntities = roleService.findAll();
		assertEquals(roleEntities, foundRoleEntities);
		
		verify(roleRepository, times(1)).findAll();
	}
	
	@Test
	void findById() {
		RoleEntity roleEntity = new RoleEntity("user", "user");
		
		when(roleRepository.findById(anyInt())).thenReturn(Optional.of(roleEntity));
		RoleEntity foundRoleEntity = roleService.findById(1);
		assertEquals(roleEntity, foundRoleEntity);
		
		verify(roleRepository, times(1)).findById(1);
	}
	
	@Test
	void findByCode() {
		RoleEntity roleEntity = new RoleEntity("user", "user");
		
		when(roleRepository.findByCode(anyString())).thenReturn(roleEntity);
		RoleEntity foundRoleEntity = roleService.findByCode("user");
		assertEquals(roleEntity, foundRoleEntity);
		
		verify(roleRepository, times(1)).findByCode("user");
	}
	
	@Test
	void save() {
		RoleEntity roleEntity = new RoleEntity("user", "user");
		
		when(roleRepository.save(any(RoleEntity.class))).thenReturn(roleEntity);
		RoleEntity savedRoleEntity = roleService.save(roleEntity);
		assertEquals(roleEntity, savedRoleEntity);
		
		verify(roleRepository, times(1)).save(roleEntity);
	}
	
	@Test
	void deleteRole() {
		roleService.deleteRole(anyInt());
		verify(roleRepository, times(1)).deleteById(anyInt());
	}
}