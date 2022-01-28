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
@SpringBootTest(properties = "spring.main.lazy-initialization=true",
		classes = {RoleService.class, RoleServiceImpl.class})
class RoleServiceTest {
	private final RoleService roleService;
	
	@MockBean
	private RoleRepository roleRepository;
	
	@Autowired
	RoleServiceTest(RoleService roleService) {
		this.roleService = roleService;
	}
	
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
		Optional<RoleEntity> roleEntity = Optional.of(new RoleEntity("user", "user"));
		
		when(roleRepository.findById(anyLong())).thenReturn(roleEntity);
		Optional<RoleEntity> foundRoleEntity = roleService.findById(1L);
		assertEquals(roleEntity, foundRoleEntity);
		
		verify(roleRepository, times(1)).findById(1L);
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
	void delete() {
		roleService.delete(anyLong());
		verify(roleRepository, times(1)).deleteById(anyLong());
	}
	
	@Test
	void updateRoleEntity() {
		roleRepository.updateRoleEntity(anyLong(), anyString(), anyString());
		verify(roleRepository, times(1)).updateRoleEntity(anyLong(), anyString(), anyString());
	}
	
	@Test
	void findByCode() {
		Optional<RoleEntity> roleEntity = Optional.of(new RoleEntity("user", "user"));
		
		when(roleRepository.findByCode(anyString())).thenReturn(roleEntity);
		Optional<RoleEntity> foundRoleEntity = roleService.findByCode("user");
		assertEquals(roleEntity, foundRoleEntity);
		
		verify(roleRepository, times(1)).findByCode("user");
	}
}