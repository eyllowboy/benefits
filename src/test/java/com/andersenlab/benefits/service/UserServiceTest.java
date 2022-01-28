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
@SpringBootTest(properties = "spring.main.lazy-initialization=true",
		classes = {UserService.class, UserServiceImpl.class})
class UserServiceTest {
	private final UserService userService;

	@MockBean
	private UserRepository userRepository;
	
	@Autowired
	UserServiceTest(UserService userService) {
		this.userService = userService;
	}
	
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

		when(userRepository.findById(anyLong())).thenReturn(Optional.of(userEntity));
		UserEntity foundUserEntity = userService.findById(1L).get();
		assertEquals(userEntity, foundUserEntity);

		verify(userRepository, times(1)).findById(1L);
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
	void delete() {
		userService.delete(anyLong());
		verify(userRepository, times(1)).deleteById(anyLong());
	}
	
	@Test
	void findByLogin() {
		Optional<UserEntity> userEntity =
				Optional.of(new UserEntity("user", new RoleEntity("user", "user")));

		when(userRepository.findByLogin(anyString())).thenReturn(userEntity);
		Optional<UserEntity> foundUserEntity = userService.findByLogin("u");
		assertEquals(userEntity, foundUserEntity);

		verify(userRepository, times(1)).findByLogin("u");
	}
	
	@Test
	void updateUserEntity() {
		RoleEntity roleEntity = new RoleEntity(1L, "abc", "def");
		userRepository.updateUserEntity(1L, "abc", roleEntity);
		verify(userRepository, times(1))
				.updateUserEntity(1L, "abc", roleEntity);
	}
}