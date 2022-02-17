package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.service.LocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.util.NestedServletException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.xml.stream.Location;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@WithMockUser
public class UserControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private LocationService locationService;

	@Container
	public static final PostgreSQLContainer<?> postgreSQLContainer =
			new PostgreSQLContainer<>("postgres")
					.withDatabaseName("benefits")
					.withUsername("benefits")
					.withPassword("ben0147");
	
	@DynamicPropertySource
	public static void postgreSQLProperties(final DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
	}
	
	@Test
	public void whenGetAllUsers() throws Exception {
		// when
		mockMvc.perform(MockMvcRequestBuilders
						.get("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
		// then
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", notNullValue()));
	}
	
	@Test
	public void whenGetUserByIdAndIdExists() throws Exception {
		// when
		mockMvc.perform(MockMvcRequestBuilders
						.get("/users/{id}", 1L)
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
		// then
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", notNullValue()))
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.login", is("admin")))
				.andExpect(jsonPath("$.roleEntity.id", is(1)))
				.andExpect(jsonPath("$.roleEntity.name", is("System administrator")))
				.andExpect(jsonPath("$.roleEntity.code", is("ROLE_ADMIN")));
	}
	
	@Test
	public void whenGetUserByIdAndIdNotExists() {
		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(get("/users/{id}", Long.MAX_VALUE)
						.with(csrf())));
		
		// then
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		assertEquals("User with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	public void whenAddUserIsSuccess() throws Exception {
		// when
		mockMvc.perform(MockMvcRequestBuilders
						.post("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.param("login", "login_1")
						.param("roleId", "1"))
				.andDo(print())
		// then
				.andExpect(jsonPath("$", notNullValue()))
				.andExpect(jsonPath("$.id", isA(Number.class)))
				.andExpect(jsonPath("$.login", is("login_1")))
				.andExpect(jsonPath("$.roleEntity.id", is(1)))
				.andExpect(jsonPath("$.roleEntity.name", is("System administrator")))
				.andExpect(jsonPath("$.roleEntity.code", is("ROLE_ADMIN")))
				.andExpect(jsonPath("$.location.country", is("Россия")))
				.andExpect(jsonPath("$.location.city", is("Москва")));
	}
	
	@Test
	public void whenAddUserAndLoginIsExists() {
		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(MockMvcRequestBuilders
						.post("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.param("login", "admin")
						.param("roleId", "1")
						.param("locationId", "1")));
		
		// then
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		assertEquals("User with such 'login' is already exists",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	public void whenAddUserAndRoleIsNotExists() {
		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(MockMvcRequestBuilders
						.post("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.param("login", "admin_2")
						.param("roleId", "9223372036854775807")
						.param("locationId", "1")));
		
		// then
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		assertEquals("Role with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	public void whenUpdateUserWithNewLoginAndRoleIdIsExists() throws Exception {
		// given
		final RoleEntity roleEntity = new RoleEntity(6L, "incorrect_name_1", "incorrect_role_code_1");
		final LocationEntity location = locationService.findById(1L).orElseThrow();
		final UserEntity userEntity = new UserEntity(5L, "new_login_1", roleEntity, location);
		final String roleEntityAsJsonString = new ObjectMapper().writeValueAsString(userEntity);
		
		// when
		mockMvc.perform(MockMvcRequestBuilders
						.put("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(roleEntityAsJsonString))
				.andDo(print())
		// then
				.andExpect(status().isOk());
	}
	
	@Test
	public void whenUpdateUserAndLoginIsExists() throws Exception {
		// given
		final RoleEntity roleEntity = new RoleEntity(6L, "incorrect_name_1", "incorrect_role_code_1");
		final LocationEntity location = new LocationEntity("Россия", "Уфа");
		final UserEntity userEntity = new UserEntity(5L, "admin", roleEntity, location);
		final String roleEntityAsJsonString = new ObjectMapper().writeValueAsString(userEntity);
		
		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(MockMvcRequestBuilders
						.put("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(roleEntityAsJsonString)));
		
		// then
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		assertEquals("User with such 'login' is already exists",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	public void whenUpdateUserAndRoleIsNotExists() throws Exception {
		// given
		final RoleEntity roleEntity = new RoleEntity(Long.MAX_VALUE, "incorrect_name_1", "incorrect_role_code_1");
		final LocationEntity location = new LocationEntity("Россия", "Уфа");
		final UserEntity userEntity = new UserEntity(5L, "new_login_2", roleEntity, location);
		final String roleEntityAsJsonString = new ObjectMapper().writeValueAsString(userEntity);
		
		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(MockMvcRequestBuilders
						.put("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(roleEntityAsJsonString)));
		
		// then
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		assertEquals("Role with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	public void whenUpdateUserAndIdNotExists() throws Exception {
		// given
		final RoleEntity roleEntity = new RoleEntity(1L, "incorrect_name_1", "incorrect_role_code_1");
		final LocationEntity location = new LocationEntity("Россия", "Уфа");
		final UserEntity userEntity = new UserEntity(Long.MAX_VALUE, "new_login_3", roleEntity, location);
		final String roleEntityAsJsonString = new ObjectMapper().writeValueAsString(userEntity);
		
		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(MockMvcRequestBuilders
						.put("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(roleEntityAsJsonString)));
		
		// then
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		assertEquals("User with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	public void whenDeleteUserIsSuccess() throws Exception {
		// when
		mockMvc.perform(MockMvcRequestBuilders
						.delete("/users/{id}", 7L)
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
		// then
				.andExpect(status().isOk());
	}
	
	@Test
	public void whenDeleteUserAndIdNotExists() {
		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> mockMvc.perform(MockMvcRequestBuilders
						.delete("/users/{id}", Long.MAX_VALUE)
						.with(csrf())));
		
		// then
		assertEquals(IllegalStateException.class,
				nestedServletException.getCause().getClass());
		assertEquals("User with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}
}