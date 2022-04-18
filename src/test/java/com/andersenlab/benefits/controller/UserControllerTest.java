package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.CompanyEntity;
import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.*;
import com.andersenlab.benefits.support.RestResponsePage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

import static java.lang.Math.random;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@WithMockUser
public class UserControllerTest {
	private final MockMvc mockMvc;
	private final ObjectMapper objectMapper;
	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final ControllerTestUtils ctu;

	@Autowired
	public UserControllerTest(final MockMvc mockMvc,
							  final ObjectMapper objectMapper,
							  final RoleRepository roleRepository,
							  final UserRepository userRepository,
							  final ControllerTestUtils ctu) {
		this.mockMvc = mockMvc;
		this.objectMapper = objectMapper;
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
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
	public void whenGetSomeSizeAllUsers() throws Exception {
		// given
		final int rndSize = (int) (random() * (5 - 1) + 1);
		Page<UserEntity> foundUsers = userRepository.findAll(PageRequest.of(0, rndSize));
		final MvcResult result;
		// when
		result =this.mockMvc.perform(MockMvcRequestBuilders
					.get("/users?page=0&size="+rndSize)
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
				.andReturn();
		// then
		final RestResponsePage<UserEntity> pageResult = objectMapper.readValue(result.getResponse().getContentAsString(),
				new TypeReference<>() {});
		assertEquals(200, result.getResponse().getStatus());
		assertEquals(foundUsers, pageResult);
	}

	@Test
	public void whenGetUserByIdAndIdExists() throws Exception {
		// given
		final int userPos = ctu.getRndEntityPos();
		final List<UserEntity> users = this.userRepository.saveAll(ctu.getUserList());
		final MvcResult result;

		// when
		result = this.mockMvc.perform(MockMvcRequestBuilders
				.get("/users/{id}", users.get(userPos).getId())
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andReturn();

		// then
		assertEquals(200, result.getResponse().getStatus());
		assertEquals(users.get(userPos), ctu.getUserFromJson(new JSONObject(result.getResponse().getContentAsString())));
	}

	@Test
	public void whenGetUserByIdAndIdNotExists() {
		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(get("/users/{id}", Long.MAX_VALUE).with(csrf())));
		
		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("User with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}

	@Test
	public void whenAddUserIsSuccess() throws Exception {
		// given
		final UserEntity user = ctu.getUser(ctu.getRndEntityPos());
		final MvcResult result;

		// when
		result = this.mockMvc.perform(MockMvcRequestBuilders
				.post("/users")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.param("login", user.getLogin())
				.param("roleId", user.getRoleEntity().getId().toString())
				.param("locationId", user.getLocation().getId().toString()))
				.andDo(print())
				.andReturn();
		// then
		assertEquals(201, result.getResponse().getStatus());
		assertEquals(1, this.userRepository.findAll().size());
		assertEquals(user, ctu.getUserFromJson(new JSONObject(result.getResponse().getContentAsString())));
	}

	@Test
	public void whenAddUserAndLoginIsExists() {
		// given
		final int listLength = 10;
		final UserEntity user = this.userRepository.save(ctu.getUser((int) (random() * (listLength - 1) + 1)));

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.post("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.param("login", user.getLogin())
						.param("roleId", user.getRoleEntity().getId().toString())
						.param("locationId", user.getLocation().getId().toString())));
		
		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("User with such 'login' is already exists",
				nestedServletException.getCause().getMessage());
	}

	@Test
	public void whenAddUserAndRoleIsNotExists() {
		// given
		final RoleEntity role = this.roleRepository.save(ctu.getRole(ctu.getRndEntityPos()));
		final LocationEntity location = ctu.getLocation(ctu.getRndEntityPos());

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.post("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.param("login", "userLogin")
						.param("roleId", Long.toString(role.getId() + 1))
						.param("locationId", Long.toString(location.getId()))));
		
		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("Role with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}

	@Test
	public void whenUpdateUserWithNewLoginAndRoleIdIsExists() throws Exception {
		// given
		final Long id = this.userRepository.save(ctu.getUser(ctu.getRndEntityPos())).getId();
		final UserEntity user = ctu.getUser(ctu.getRndEntityPos());
		user.setId(id);
		user.setLogin("newUserLogin");
		final MvcResult result;

		// when
		result = this.mockMvc.perform(MockMvcRequestBuilders
				.put("/users")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(user)))
				.andDo(print())
				.andReturn();
		// then
		assertEquals(200, result.getResponse().getStatus());
		assertEquals(1, this.userRepository.findAll().size());
		assertEquals(user, this.userRepository.findById(id).orElseThrow());
	}

	@Test
	public void whenUpdateUserAndLoginIsExists() {
		// given
		final List<UserEntity> users = this.userRepository.saveAll(ctu.getUserList());
		final UserEntity userSetLoginTo = users.get(users.size() - 1);
		userSetLoginTo.setLogin(users.get(0).getLogin());

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.put("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(userSetLoginTo))));
		
		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("User with such 'login' is already exists",
				nestedServletException.getCause().getMessage());
	}

	@Test
	public void whenUpdateUserAndRoleIsNotExists() {
		// given
		final UserEntity user = this.userRepository.save(ctu.getUser(ctu.getRndEntityPos()));
		final RoleEntity role = ctu.getRole(Long.MAX_VALUE);
		role.setId(Long.MAX_VALUE);
		user.setRoleEntity(role);

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.put("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(user))));
		
		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("Role with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}

	@Test
	public void whenUpdateUserAndIdNotExists() {
		// given
		final UserEntity user = this.userRepository.saveAll(ctu.getUserList()).get(0);
		user.setId(Long.MAX_VALUE);
		
		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.put("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(user))));
		
		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("User with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}

	@Test
	public void whenDeleteUserIsSuccess() throws Exception {
		// given
		final List<UserEntity> users = this.userRepository.saveAll(ctu.getUserList());
		final UserEntity userForDelete = users.get(ctu.getRndEntityPos());
		final MvcResult result;

		// when
		result = this.mockMvc.perform(MockMvcRequestBuilders
						.delete("/users/{id}", userForDelete.getId())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andReturn();

		// then
		assertEquals(200, result.getResponse().getStatus());
		assertEquals(Optional.empty(), this.userRepository.findById(userForDelete.getId()));
		assertEquals(users.size() - 1, this.userRepository.findAll().size());
	}

	@Test
	public void whenDeleteUserAndIdNotExists() {
		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
					.delete("/users/{id}", Long.MAX_VALUE)
					.with(csrf())));
		
		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("User with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}
}
