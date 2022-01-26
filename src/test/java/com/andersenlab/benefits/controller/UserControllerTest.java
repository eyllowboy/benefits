package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.util.NestedServletException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class UserControllerTest {
	@Autowired
	private MockMvc mockMvc;
	
	@Container
	public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres")
			.withDatabaseName("benefits")
			.withUsername("benefits")
			.withPassword("ben0147");
	
	@DynamicPropertySource
	public static void postgreSQLProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
	}
	
	@Test
	void getUsers() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders
						.get("/users")
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", notNullValue()));
	}
	
	@Test
	void getUserPositiveScenario() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders
						.get("/users/{id}", 1L)
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", notNullValue()))
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.login", is("admin")))
				.andExpect(jsonPath("$.roleEntity.id", is(1)))
				.andExpect(jsonPath("$.roleEntity.name", is("System administrator")))
				.andExpect(jsonPath("$.roleEntity.code", is("ROLE_ADMIN")));
	}
	
	@Test
	void getUserWithIncorrectId() {
		NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(get("/users/{id}", Long.MAX_VALUE)));
		
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		
		assertEquals("User with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	void addUserPositiveScenario() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders
						.post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.param("login", "login_1")
						.param("roleId", "1"))
				.andDo(print())
				.andExpect(jsonPath("$", notNullValue()))
				.andExpect(jsonPath("$.id", isA(Number.class)))
				.andExpect(jsonPath("$.login", is("login_1")))
				.andExpect(jsonPath("$.roleEntity.id", is(1)))
				.andExpect(jsonPath("$.roleEntity.name", is("System administrator")))
				.andExpect(jsonPath("$.roleEntity.code", is("ROLE_ADMIN")));
	}
	
	@Test
	void addUserWithIncorrectLogin() {
		NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(MockMvcRequestBuilders
						.post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.param("login", "admin")
						.param("roleId", "1")));
		
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		
		assertEquals("User with such 'login' is already exists",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	void addUserWithIncorrectRole() {
		NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(MockMvcRequestBuilders
						.post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.param("login", "admin_2")
						.param("roleId", "9223372036854775807")));
		
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		
		assertEquals("Role with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	void updateUserPositiveScenarioWithUpdateNameAndCode() throws Exception {
		RoleEntity roleEntity = new RoleEntity(6L, "incorrect_name_1", "incorrect_role_code_1");
		UserEntity userEntity = new UserEntity(5L, "new_login_1", roleEntity);
		String roleEntityAsJsonString = new ObjectMapper().writeValueAsString(userEntity);
		
		mockMvc.perform(MockMvcRequestBuilders
						.put("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(roleEntityAsJsonString))
				.andDo(print())
				.andExpect(status().isOk());
	}
	
	@Test
	void updateUserWithIncorrectLogin() throws Exception {
		RoleEntity roleEntity = new RoleEntity(6L, "incorrect_name_1", "incorrect_role_code_1");
		UserEntity userEntity = new UserEntity(5L, "admin", roleEntity);
		String roleEntityAsJsonString = new ObjectMapper().writeValueAsString(userEntity);
		
		NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(MockMvcRequestBuilders
						.put("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(roleEntityAsJsonString)));
		
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		
		assertEquals("User with such 'login' is already exists",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	void updateUserWithIncorrectRole() throws Exception {
		RoleEntity roleEntity = new RoleEntity(Long.MAX_VALUE, "incorrect_name_1", "incorrect_role_code_1");
		UserEntity userEntity = new UserEntity(5L, "new_login_2", roleEntity);
		String roleEntityAsJsonString = new ObjectMapper().writeValueAsString(userEntity);
		
		NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(MockMvcRequestBuilders
						.put("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(roleEntityAsJsonString)));
		
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		
		assertEquals("Role with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	void updateUserWithIncorrectId() throws Exception {
		RoleEntity roleEntity = new RoleEntity(1L, "incorrect_name_1", "incorrect_role_code_1");
		UserEntity userEntity = new UserEntity(Long.MAX_VALUE, "new_login_3", roleEntity);
		String roleEntityAsJsonString = new ObjectMapper().writeValueAsString(userEntity);
		
		NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(MockMvcRequestBuilders
						.put("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(roleEntityAsJsonString)));
		
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		
		assertEquals("User with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	void deleteUserPositiveScenario() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders
						.delete("/users/{id}", 7L)
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk());
	}
	
	@Test
	void deleteUserWithIncorrectId() {
		NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(MockMvcRequestBuilders
						.delete("/users/{id}", Long.MAX_VALUE)));
		
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		
		assertEquals("User with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}
}