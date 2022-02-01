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
	public RoleServiceTest(final RoleService roleService) {
		this.roleService = roleService;
	}
	
	@Test
	public void  whenFindAll() {
		// given
		final List<RoleEntity> roleEntities = List.of(
				new RoleEntity("user", "user"),
				new RoleEntity("user1", "user1"),
				new RoleEntity("user2", "user2"));
		
		// when
		when(roleRepository.findAll()).thenReturn(roleEntities);
		final List<RoleEntity> foundRoleEntities = roleService.findAll();
		
		// then
		assertEquals(roleEntities, foundRoleEntities);
		verify(roleRepository, times(1)).findAll();
	}
	
	@Test
	public void whenFindById() {
		// given
		final Optional<RoleEntity> roleEntity = Optional.of(new RoleEntity("user", "user"));
		
		// when
		when(roleRepository.findById(anyLong())).thenReturn(roleEntity);
		final Optional<RoleEntity> foundRoleEntity = roleService.findById(1L);
		
		// then
		assertEquals(roleEntity, foundRoleEntity);
		verify(roleRepository, times(1)).findById(1L);
	}
	
	@Test
	public void whenSave() {
		// given
		final RoleEntity roleEntity = new RoleEntity("user", "user");
		
		// when
		when(roleRepository.save(any(RoleEntity.class))).thenReturn(roleEntity);
		final RoleEntity savedRoleEntity = roleService.save(roleEntity);
		
		// then
		assertEquals(roleEntity, savedRoleEntity);
		verify(roleRepository, times(1)).save(roleEntity);
	}
	
	@Test
	public void whenDelete() {
		// when
		roleService.delete(anyLong());
		
		// then
		verify(roleRepository, times(1)).deleteById(anyLong());
	}
	
	@Test
	public void whenUpdate() {
		// when
		roleRepository.updateRoleEntity(anyLong(), anyString(), anyString());
		
		// then
		verify(roleRepository, times(1)).updateRoleEntity(anyLong(), anyString(), anyString());
	}
	
	@Test
	public void whenFindByCode() {
		// given
		final Optional<RoleEntity> roleEntity = Optional.of(new RoleEntity("user", "user"));
		
		// when
		when(roleRepository.findByCode(anyString())).thenReturn(roleEntity);
		final Optional<RoleEntity> foundRoleEntity = roleService.findByCode("user");
		
		// then
		assertEquals(roleEntity, foundRoleEntity);
		verify(roleRepository, times(1)).findByCode("user");
	}
}