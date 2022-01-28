package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class UserServiceDBTest {
	private final UserService userService;
	private final RoleService roleService;
	
	@Autowired
	UserServiceDBTest(UserService userService, RoleService roleService) {
		this.userService = userService;
		this.roleService = roleService;
	}
	
	@Container
	public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres")
			.withDatabaseName("benefits")
			.withUsername("benefits")
			.withPassword("ben0147");
	
	@DynamicPropertySource
	static void postgreSQLProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
	}

	@Test
	void findAll() {
		userService.findAll().forEach(System.out::println);
	}

	@Test
	void findById() {
		System.out.println(userService.findById(1L));;
	}

	@Test
	void save() {
		RoleEntity roleEntity = roleService.findById(1L).orElse(new RoleEntity());
		UserEntity log1 = new UserEntity("log", roleEntity);
		UserEntity save1 = userService.save(log1);
		System.out.println(save1);
	}
	
	@Test
	void delete() {
		userService.delete(2L);
	}
	
	@Test
	void findByLogin() {
		System.out.println(userService.findByLogin("admin"));;
	}
	
	@Test
	void updateUserEntity() {
		UserEntity byId = userService.findById(5L).orElseThrow();
		System.out.println(byId);
		byId.setLogin("iou");
		userService.updateUserEntity(5L, "yo-ho-ho", roleService.findById(1L).orElseThrow());
		System.out.println(userService.findById(5L).orElseThrow());
	}
}
