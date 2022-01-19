package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
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
class UserServiceTest {
	@Autowired
	private UserService userService;
	
	@MockBean
	private UserRepository userRepository;
	
	@Test
	void findAll() {
		List<UserEntity> userEntities = List.of(
				new UserEntity("user", new RoleEntity("user", "user")),
				new UserEntity("user1", new RoleEntity("user1", "user1")),
				new UserEntity("user2", new RoleEntity("user2", "user2")));
		
		when(userRepository.findAll()).thenReturn(userEntities);
		List<UserEntity> foundUserEntities = userService.findAll();
		assertEquals(userEntities, foundUserEntities);
		
		verify(userRepository, times(1)).findAll();
	}
	
	@Test
	void findById() {
		UserEntity userEntity = new UserEntity("user", new RoleEntity("user", "user"));
		
		when(userRepository.findById(anyInt())).thenReturn(Optional.of(userEntity));
		UserEntity foundUserEntity = userService.findById(1);
		assertEquals(userEntity, foundUserEntity);
		
		verify(userRepository, times(1)).findById(1);
	}
	
	@Test
	void findByLogin() {
		UserEntity userEntity = new UserEntity("user", new RoleEntity("user", "user"));
		
		when(userRepository.findByLogin(anyString())).thenReturn(userEntity);
		UserEntity foundUserEntity = userService.findByLogin("u");
		assertEquals(userEntity, foundUserEntity);
		
		verify(userRepository, times(1)).findByLogin("u");
	}
	
	@Test
	void save() {
		UserEntity userEntity = new UserEntity("user", new RoleEntity("user", "user"));
		
		when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
		UserEntity foundUserEntity = userService.save(userEntity);
		assertEquals(userEntity, foundUserEntity);
		
		verify(userRepository, times(1)).save(userEntity);
	}
	
	@Test
	void saveNull() {
		Assertions.assertThrows(BenefitsServiceException.class,
				() ->  userService.save(new UserEntity("user", null)));
	}
	
	@Test
	void deleteUser() {
		userService.deleteUser(anyInt());
		verify(userRepository, times(1)).deleteById(anyInt());
	}
}