package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.RoleEntity;
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
class RoleControllerTest {
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
	void getRoles() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders
						.get("/roles")
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", notNullValue()));
	}
	
	@Test
	void getRolePositiveScenario() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders
						.get("/roles/{id}", 1L)
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", notNullValue()))
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.name", is("System administrator")))
				.andExpect(jsonPath("$.code", is("ROLE_ADMIN")));
	}
	
	@Test
	void getRoleWithIncorrectId() {
		NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(get("/roles/{id}", Long.MAX_VALUE)));
		
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		
		assertEquals("Role with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	void addRolePositiveScenario() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders
						.post("/roles")
						.contentType(MediaType.APPLICATION_JSON)
						.param("name", "new_name_1")
						.param("code", "new_role_code_1"))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$", notNullValue()))
				.andExpect(jsonPath("$.id", isA(Number.class)))
				.andExpect(jsonPath("$.name", is("new_name_1")))
				.andExpect(jsonPath("$.code", is("new_role_code_1")));
	}
	
	@Test
	void addRoleWithIncorrectCode() {
		NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(MockMvcRequestBuilders
						.post("/roles")
						.contentType(MediaType.APPLICATION_JSON)
						.param("name", "new_name_2")
						.param("code", "ROLE_ADMIN")));
		
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		
		assertEquals("Role with such 'code' is already exists",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	void updateRolePositiveScenarioWithUpdateNameAndCode() throws Exception {
		RoleEntity roleEntity = new RoleEntity(5L, "name", "new_role_code_2");
		String roleEntityAsJsonString = new ObjectMapper().writeValueAsString(roleEntity);
		
		mockMvc.perform(MockMvcRequestBuilders
						.put("/roles")
						.contentType(MediaType.APPLICATION_JSON)
						.content(roleEntityAsJsonString))
				.andDo(print())
				.andExpect(status().isOk());
	}
	
	@Test
	void updateRolePositiveScenarioWithUpdateName() throws Exception {
		RoleEntity roleEntity = new RoleEntity(2L, "name", "ROLE_MODERATOR");
		String roleEntityAsJsonString = new ObjectMapper().writeValueAsString(roleEntity);
		
		mockMvc.perform(MockMvcRequestBuilders
						.put("/roles")
						.contentType(MediaType.APPLICATION_JSON)
						.content(roleEntityAsJsonString))
				.andDo(print())
				.andExpect(status().isOk());
	}
	
	@Test
	void updateRoleWithIncorrectCode() throws Exception {
		RoleEntity roleEntity = new RoleEntity(1L, "name", "ROLE_USER");
		String roleEntityAsJsonString = new ObjectMapper().writeValueAsString(roleEntity);
		
		NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(MockMvcRequestBuilders
						.put("/roles")
						.contentType(MediaType.APPLICATION_JSON)
						.content(roleEntityAsJsonString)));
		
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		
		assertEquals("Role with such 'code' is already exists",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	void updateRoleWithIncorrectId() throws Exception {
		RoleEntity roleEntity = new RoleEntity(Long.MAX_VALUE, "name", "code");
		String roleEntityAsJsonString = new ObjectMapper().writeValueAsString(roleEntity);
		
		NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(MockMvcRequestBuilders
						.put("/roles")
						.contentType(MediaType.APPLICATION_JSON)
						.content(roleEntityAsJsonString)));
		
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		
		assertEquals("Role with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	void deleteRolePositiveScenario() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders
						.delete("/roles/{id}", 10L)
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk());
	}
	
	@Test
	void deleteRoleWithIncorrectId() {
		NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(MockMvcRequestBuilders
						.delete("/roles/{id}", Long.MAX_VALUE)));
		
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		
		assertEquals("Role with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}
}