package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.RoleEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest()
class RoleServiceDBTest {
	private final RoleService roleService;
	
	@Autowired
	RoleServiceDBTest(RoleService roleServiceImpl) {
		this.roleService = roleServiceImpl;
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
		roleService.findAll().forEach(System.out::println);
	}
	
	@Test
	void findById() {
		System.out.println(roleService.findById(1L).orElse(null));
	}
	
	@Test
	void save() {
		RoleEntity roleEntity = new RoleEntity("sys", "romul");
		RoleEntity save = roleService.save(roleEntity);
		System.out.println(save);
	}
	
	@Test
	void delete() {
		System.out.println(roleService.findAll().size());
		roleService.delete(10L);
		System.out.println(roleService.findAll().size());
	}
	
	@Test
	void updateRoleEntity() {
		System.out.println(roleService.findById(2L));
		roleService.updateRoleEntity(2L, "sys", "romulus");
		System.out.println(roleService.findById(2L));
	}
	
	@Test
	void findByCode() {
		System.out.println(roleService.findByCode("ROLE_ADMIN"));
	}
}