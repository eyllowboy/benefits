package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@WithMockUser
public class RoleControllerTest {
	private final MockMvc mockMvc;
	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;
	private final ControllerTestUtils ctu;

	@Autowired
	public RoleControllerTest(final MockMvc mockMvc,
							  final UserRepository userRepository,
							  final RoleRepository roleRepository,
							  final ObjectMapper objectMapper,
							  final ControllerTestUtils ctu) {
		this.mockMvc = mockMvc;
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.objectMapper = objectMapper;
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
		this.ctu.clearTables();
	}

	@Test
	public void whenGetAllRolesSuccess() throws Exception {
		// given
		final List<RoleEntity> roles = this.roleRepository.saveAll(ctu.getRoleList());

		// when
		this.mockMvc.perform(MockMvcRequestBuilders
						.get("/roles?page=0&size=3")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				// then
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", notNullValue()))
				.andExpect(jsonPath("$.number", is(0)))
				.andExpect(jsonPath("$.size", is(3)));

	}
	
	@Test
	public void whenGetRoleByIdSuccess() throws Exception {
		// given
		final int rolePos = this.ctu.getRndEntityPos();
		final List<RoleEntity> roles = this.roleRepository.saveAll(this.ctu.getRoleList());
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
		assertEquals(roles.get(rolePos), this.ctu.getRoleFromJson(new JSONObject(result.getResponse().getContentAsString())));
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
		final RoleEntity role = this.ctu.getRole(this.ctu.getRndEntityPos());
		final MvcResult result;

		// when
		result = this.mockMvc.perform(MockMvcRequestBuilders
						.post("/roles")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(role)))
				.andReturn();

		// then
		assertEquals(201, result.getResponse().getStatus());
		assertEquals(1, this.roleRepository.findAll().size());
		assertEquals(role, this.ctu.getRoleFromJson(new JSONObject(result.getResponse().getContentAsString())));
	}
	
	@Test
	public void whenAddRoleAndCodeIsExists() {
		// given
		final RoleEntity role = this.roleRepository.save(this.ctu.getRole(this.ctu.getRndEntityPos()));

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.post("/roles")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(role))));

		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("Role with such 'code' is already exists",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	public void whenUpdateRoleWithNewNameAndCodeIsSuccess() throws Exception {
		// given
		final List<RoleEntity> roles = this.roleRepository.saveAll(this.ctu.getRoleList());
		final RoleEntity role = roles.get(this.ctu.getRndEntityPos());
		role.setName("newRoleName");
		role.setCode("newRoleCode");
		final MvcResult result;

		// when
		result = this.mockMvc.perform(MockMvcRequestBuilders
				.patch("/roles/{id}", role.getId())
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(role)))
				.andDo(print())
				.andReturn();

		// then
		assertEquals(200, result.getResponse().getStatus());
		assertEquals(role, this.roleRepository.findById(role.getId()).orElseThrow());
	}

	@Test
	public void whenUpdateRoleAndCodeIsExists() {
		// given
		final List<RoleEntity> roles = this.roleRepository.saveAll(this.ctu.getRoleList());
		final RoleEntity role = roles.get(roles.size() - 1);
		role.setCode(roles.get(0).getCode());

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.patch("/roles/{id}", role.getId())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(role))));
		
		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("Role with such 'code' is already exists",
				nestedServletException.getCause().getMessage());
	}

	@Test
	public void whenUpdateRoleAndIdNotExists() {
		// given
		final RoleEntity role = this.roleRepository.saveAll(this.ctu.getRoleList()).get(0);
		role.setId(0L);

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.patch("/roles/{id}", role.getId())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(role))));
		
		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("Role with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}
	
	@Test
	public void whenDeleteRoleAndIdNotExists() {
		// given
		final RoleEntity role = this.roleRepository.saveAll(this.ctu.getRoleList()).get(0);
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
		final List<RoleEntity> roles = this.roleRepository.saveAll(this.ctu.getRoleList());
		final RoleEntity role = roles.get(this.ctu.getRndEntityPos());
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
		final int pos = this.ctu.getRndEntityPos();
		final List<RoleEntity> roles = this.roleRepository.saveAll(this.ctu.getRoleList());
		final RoleEntity role = roles.get(pos);
		final UserEntity user = this.ctu.getUser(pos);
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

	@ParameterizedTest
	@ValueSource(strings = {"", " ", "    "})
	public void whenAddRoleWrongObligatoryFields(final String name) throws Exception {
		final RoleEntity role = new RoleEntity(name, "someCode");
		final MvcResult result;

		// when
		result = this.mockMvc.perform(MockMvcRequestBuilders
						.post("/roles")
						.contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(role))
						.with(csrf()))
				.andReturn();

		// then
		assertEquals(400, result.getResponse().getStatus());
		final String errorResult = Objects.requireNonNull(result.getResolvedException()).getMessage();
		assertTrue(errorResult.contains("must not be blank"));
	}

	@ParameterizedTest
	@ValueSource(strings = {" space at start", "space at end ", " two  spaces  inside", " three   spaces   inside"})
	public void whenAddRoleTrimFields(final String name) throws Exception {
		final RoleEntity role = new RoleEntity(name, "someCode");
		final RoleEntity postedRole;
		String postedName;
		final MvcResult result;

		//when
		result = this.mockMvc.perform(MockMvcRequestBuilders
						.post("/roles")
						.contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(role))
						.with(csrf()))
				.andReturn();

		// then
		assertEquals(201, result.getResponse().getStatus());
		postedRole = this.ctu.getRoleFromJson(new JSONObject(result.getResponse().getContentAsString()));
		postedName = name.trim();
		while (postedName.contains("  "))
			postedName = postedName.replace("  ", " ");
		assertEquals(postedName, postedRole.getName());
	}

	@ParameterizedTest
	@ValueSource(strings = {"0", "150"})
	public void whenAddRoleWrongFieldSize(final Integer stringSize) throws Exception {
		// given
		final String fieldValue = "a".repeat(stringSize);
		final RoleEntity role = this.ctu.getRole(this.ctu.getRndEntityPos());
		role.setName(fieldValue);
		final MvcResult result;

		// when
		result = this.mockMvc.perform(MockMvcRequestBuilders
						.post("/roles")
						.contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(role))
						.with(csrf()))
				.andReturn();

		// then
		assertEquals(400, result.getResponse().getStatus());
		final String errorResult = Objects.requireNonNull(result.getResolvedException()).getMessage();
		assertTrue(errorResult.contains("must be between"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"50", "150"})
	public void whenUpdateRoleWrongFieldSize(final Integer stringSize) throws Exception {
		// given
		final RoleEntity role = this.roleRepository.save(this.ctu.getRole(this.ctu.getRndEntityPos()));
		final String fieldValue = "a".repeat(stringSize);
		role.setName(fieldValue);
		role.setCode("New code");

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
				this.mockMvc.perform(MockMvcRequestBuilders
						.patch("/roles/{id}", role.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(role))
						.with(csrf())));

		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertTrue(nestedServletException.getCause().getMessage().contains("must be between"));
	}
}
