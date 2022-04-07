package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.util.NestedServletException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@WithMockUser
public class RoleControllerTest {
	private final MockMvc mockMvc;
	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final ControllerTestUtils ctu;

	@Autowired
	public RoleControllerTest(final MockMvc mockMvc,
							  final UserRepository userRepository,
							  final RoleRepository roleRepository,
							  final ControllerTestUtils ctu) {
		this.mockMvc = mockMvc;
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.ctu = ctu;
	}
	@Container
	public static final PostgreSQLContainer<?> postgreSQLContainer =
			new PostgreSQLContainer<>("postgres")
					.withDatabaseName("benefits")
					.withUsername("benefits")
					.withPassword("ben0147");
	
	@DynamicPropertySource
	static void postgreSQLProperties(final DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
	}

	@BeforeEach
	public void clearData() {
		ctu.clearTables();
	}

	@Test
	public void whenGetAllRolesSuccess() throws Exception {
		// given
		final List<RoleEntity> roles = this.roleRepository.saveAll(ctu.getRoleList());
		final MvcResult result;
		final List<RoleEntity> rolesResult;

		// when
		result = this.mockMvc.perform(MockMvcRequestBuilders
						.get("/roles")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andReturn();

		// then
		assertEquals(200, result.getResponse().getStatus());
		rolesResult = ctu.getRolesFromJson(result.getResponse().getContentAsString());
		rolesResult.forEach(item -> assertTrue(roles.contains(item)));
	}
	
	@Test
	public void whenGetRoleByIdSuccess() throws Exception {
		// given
		final int rolePos = ctu.getRndEntityPos();
		final List<RoleEntity> roles = this.roleRepository.saveAll(ctu.getRoleList());
		final MvcResult result;

		// when
		result = this.mockMvc.perform(MockMvcRequestBuilders
						.get("/roles/{id}", roles.get(rolePos).getId())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andReturn();

		// then
		assertEquals(200, result.getResponse().getStatus());
		assertEquals(roles.get(rolePos), ctu.getRoleFromJson(new JSONObject(result.getResponse().getContentAsString())));
	}
	
	@Test
	public void whenGetRoleByIdFailIdNotExists() {
		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(get("/roles/{id}", Long.MAX_VALUE).with(csrf())));
		
		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("Role with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	public void whenAddRoleSuccess() throws Exception {
		// given
		final RoleEntity role = ctu.getRole(ctu.getRndEntityPos());
		final MvcResult result;

		// when
		result = this.mockMvc.perform(MockMvcRequestBuilders
						.post("/roles")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.param("name", role.getName())
						.param("code", role.getCode()))
				.andDo(print())
				.andReturn();

		// thfen
		assertEquals(201, result.getResponse().getStatus());
		assertEquals(1, this.roleRepository.findAll().size());
		assertEquals(role, ctu.getRoleFromJson(new JSONObject(result.getResponse().getContentAsString())));
	}
	
	@Test
	public void whenAddRoleAndCodeIsExists() {
		// given
		final RoleEntity role = this.roleRepository.save(ctu.getRole(ctu.getRndEntityPos()));

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.post("/roles")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.param("name", role.getName())
						.param("code", role.getCode())));
		
		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("Role with such 'code' is already exists",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	public void whenUpdateRoleWithNewNameAndCodeIsSuccess() throws Exception {
		// given
		final List<RoleEntity> roles = this.roleRepository.saveAll(ctu.getRoleList());
		final RoleEntity role = roles.get(ctu.getRndEntityPos());
		role.setName("newRoleName");
		role.setCode("newRoleCode");
		final MvcResult result;

		// when
		result = this.mockMvc.perform(MockMvcRequestBuilders
				.put("/roles")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(role)))
				.andDo(print())
				.andReturn();

		// then
		assertEquals(200, result.getResponse().getStatus());
		assertEquals(role, this.roleRepository.findById(role.getId()).orElseThrow());
	}

	@Test
	public void whenUpdateRoleAndCodeIsExists() {
		// given
		final List<RoleEntity> roles = this.roleRepository.saveAll(ctu.getRoleList());
		final RoleEntity role = roles.get(roles.size() - 1);
		role.setCode(roles.get(0).getCode());

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.put("/roles")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(role))));
		
		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("Role with such 'code' is already exists",
				nestedServletException.getCause().getMessage());
	}

	@Test
	public void whenUpdateRoleAndIdNotExists() {
		// given
		final RoleEntity role = this.roleRepository.saveAll(ctu.getRoleList()).get(0);
		role.setId(0L);

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.put("/roles")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(role))));
		
		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("Role with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	public void whenDeleteRoleAndIdNotExists() {
		// given
		final RoleEntity role = this.roleRepository.saveAll(ctu.getRoleList()).get(0);
		role.setId(0L);

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.delete("/roles/{id}", role.getId())
						.with(csrf())));
		
		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("Role with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}

	@Test
	public void whenDeleteRoleWithoutUsersSuccess() throws Exception {
		// given
		final List<RoleEntity> roles = this.roleRepository.saveAll(ctu.getRoleList());
		final RoleEntity role = roles.get(ctu.getRndEntityPos());
		final MvcResult result;

		// when
		result = this.mockMvc.perform(MockMvcRequestBuilders
						.delete("/roles/{id}", role.getId())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andReturn();

		// then
		assertEquals(200, result.getResponse().getStatus());
		assertEquals(roles.size() - 1, this.roleRepository.findAll().size());
		assertEquals(Optional.empty(), this.roleRepository.findById(role.getId()));
	}

	@Test
	public void whenDeleteRoleFailHasActiveUsers() {
		// given
		final int pos = ctu.getRndEntityPos();
		final List<RoleEntity> roles = this.roleRepository.saveAll(ctu.getRoleList());
		final RoleEntity role = roles.get(pos);
		final UserEntity user = ctu.getUser(pos);
		user.setRoleEntity(this.roleRepository.findById(role.getId()).orElseThrow());
		this.userRepository.save(user);

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.delete("/roles/{id}", role.getId())
						.with(csrf())));

		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("There is active users with this Role in database",
				nestedServletException.getCause().getMessage());
	}
}
