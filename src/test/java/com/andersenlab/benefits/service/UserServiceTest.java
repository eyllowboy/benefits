package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.UserRepository;
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
public class UserServiceTest {
	private final UserService userService;
	
	@MockBean
	private UserRepository userRepository;
	
	@Autowired
	public UserServiceTest(final UserService userService) {
		this.userService = userService;
	}
	
	@Test
	public void whenFindAll() {
		// given
		final List<UserEntity> userEntities = List.of(
				new UserEntity("user", new RoleEntity("user", "user")),
				new UserEntity("user1", new RoleEntity("user1", "user1")),
				new UserEntity("user2", new RoleEntity("user2", "user2")));
		
		// when
		when(userRepository.findAll()).thenReturn(userEntities);
		final List<UserEntity> foundUserEntities = userService.findAll();
		
		// then
		assertEquals(userEntities, foundUserEntities);
		verify(userRepository, times(1)).findAll();
	}
	
	@Test
	public void whenFindById() {
		// given
		final UserEntity userEntity = new UserEntity("user", new RoleEntity("user", "user"));
		
		// when
		when(userRepository.findById(anyLong())).thenReturn(Optional.of(userEntity));
		final UserEntity foundUserEntity = userService.findById(1L).orElseThrow();
		
		// then
		assertEquals(userEntity, foundUserEntity);
		verify(userRepository, times(1)).findById(1L);
	}
	
	@Test
	public void whenSave() {
		// given
		final UserEntity userEntity = new UserEntity("user", new RoleEntity("user", "user"));
		
		// when
		when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
		final UserEntity foundUserEntity = userService.save(userEntity);
		
		// then
		assertEquals(userEntity, foundUserEntity);
		verify(userRepository, times(1)).save(userEntity);
	}
	
	@Test
	public void whenDelete() {
		// when
		userService.delete(anyLong());
		
		// then
		verify(userRepository, times(1)).deleteById(anyLong());
	}
	
	@Test
	public void whenFindByLogin() {
		// given
		final Optional<UserEntity> userEntity =
				Optional.of(new UserEntity("user", new RoleEntity("user", "user")));
		
		// when
		when(userRepository.findByLogin(anyString())).thenReturn(userEntity);
		final Optional<UserEntity> foundUserEntity = userService.findByLogin("u");
		
		// then
		assertEquals(userEntity, foundUserEntity);
		verify(userRepository, times(1)).findByLogin("u");
	}
	
	@Test
	public void whenUpdate() {
		// given
		final RoleEntity roleEntity = new RoleEntity(1L, "abc", "def");
		
		// when
		userRepository.updateUserEntity(1L, "abc", roleEntity);
		
		// then
		verify(userRepository, times(1))
				.updateUserEntity(1L, "abc", roleEntity);
	}
}