package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.*;
import com.andersenlab.benefits.support.RestResponsePage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import java.util.Objects;
import java.util.Optional;

import static java.lang.Math.random;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


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
		this.ctu.clearTables();
	}

	@Test
	public void whenGetSomeSizeAllUsers() throws Exception {
		// given
		final int rndSize = (int) (random() * (5 - 1) + 1);
		final Page<UserEntity> foundUsers = this.userRepository.findAll(PageRequest.of(0, rndSize));
		final MvcResult result;
		// when
		result =this.mockMvc.perform(MockMvcRequestBuilders
					.get("/users?page=0&size="+rndSize)
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
				.andReturn();
		// then
		final RestResponsePage<UserEntity> pageResult = this.objectMapper.readValue(result.getResponse().getContentAsString(),
				new TypeReference<>() {});
		assertEquals(200, result.getResponse().getStatus());
		assertEquals(foundUsers, pageResult);
	}

	@Test
	public void whenGetUserByIdAndIdExists() throws Exception {
		// given
		final int userPos = this.ctu.getRndEntityPos();
		final List<UserEntity> users = this.userRepository.saveAll(this.ctu.getUserList());
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
		assertEquals(users.get(userPos), this.ctu.getUserFromJson(new JSONObject(result.getResponse().getContentAsString())));
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
		final UserEntity user = this.ctu.getUser(this.ctu.getRndEntityPos());
		final MvcResult result;

		// when
		result = this.mockMvc.perform(MockMvcRequestBuilders
				.post("/users")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(user)))
				.andReturn();
		// then
		assertEquals(201, result.getResponse().getStatus());
		assertEquals(1, this.userRepository.findAll().size());
		assertEquals(user, this.ctu.getUserFromJson(new JSONObject(result.getResponse().getContentAsString())));
	}

	@Test
	public void whenAddUserAndLoginIsExists() {
		// given
		final UserEntity user = this.userRepository.save(this.ctu.getUser(this.ctu.getRndEntityPos()));

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.post("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(user))));

		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("User with such 'login' is already exists",
				nestedServletException.getCause().getMessage());
	}

	@Test
	public void whenAddUserAndRoleIsNotExists() {
		// given
		final UserEntity user = this.ctu.getUser(this.ctu.getRndEntityPos());
		final RoleEntity role = this.roleRepository.save(this.ctu.getRole(this.ctu.getRndEntityPos()));
		role.setId(role.getId() + 1);
		user.setRoleEntity(role);

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.post("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(user))));

		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("Role with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}

	@Test
    public void whenUpdateUserWithNewLocationAndRoleIdExists() throws Exception {
        // given
        final UserEntity user = this.userRepository.save(this.ctu.getUser(this.ctu.getRndEntityPos()));
        user.setLocation(this.ctu.getLocation(this.ctu.getRndEntityPos()));
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/users/{id}", user.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andReturn();
        // then
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(1, this.userRepository.findAll().size());
        assertEquals(user, this.userRepository.findById(user.getId()).orElseThrow());
    }

	@Test
	public void whenUpdateUserAndLoginIsExists() {
		// given
		final List<UserEntity> users = this.userRepository.saveAll(this.ctu.getUserList());
		final UserEntity userSetLoginTo = users.get(users.size() - 1);
		userSetLoginTo.setLogin(users.get(0).getLogin());

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.patch("/users/{id}", userSetLoginTo.getId())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(userSetLoginTo))));

		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("User with such 'login' is already exists",
				nestedServletException.getCause().getMessage());
	}

	@Test
	public void whenUpdateUserAndRoleIsNotExists() {
		// given
		final UserEntity user = this.userRepository.save(this.ctu.getUser(this.ctu.getRndEntityPos()));
		final RoleEntity role = this.ctu.getRole(Long.MAX_VALUE);
		role.setId(Long.MAX_VALUE);
		user.setRoleEntity(role);

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.patch("/users/{id}", user.getId())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(user))));

		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("Role with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}

	@Test
	public void whenUpdateUserAndIdNotExists() {
		// given
		final UserEntity user = this.userRepository.saveAll(this.ctu.getUserList()).get(0);
		user.setId(Long.MAX_VALUE);
		user.setLogin("New Login");

		// when
		final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
				() -> this.mockMvc.perform(MockMvcRequestBuilders
						.patch("/users/{id}", user.getId())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(user))));

		// then
		assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
		assertEquals("User with this id was not found in the database",
				nestedServletException.getCause().getMessage());
	}

	@Test
	public void whenDeleteUserIsSuccess() throws Exception {
		// given
		final List<UserEntity> users = this.userRepository.saveAll(this.ctu.getUserList());
		final UserEntity userForDelete = users.get(this.ctu.getRndEntityPos());
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

	@ParameterizedTest
	@ValueSource(strings = {"", " ", "    "})
	public void whenAddUserWrongObligatoryFields(final String login) throws Exception {
		final UserEntity user = this.ctu.getUser(this.ctu.getRndEntityPos());
		user.setLogin(login);
		final MvcResult result;

		// when
		result = this.mockMvc.perform(MockMvcRequestBuilders
						.post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(user))
						.with(csrf()))
				.andReturn();

		// then
		assertEquals(400, result.getResponse().getStatus());
		final String errorResult = Objects.requireNonNull(result.getResolvedException()).getMessage();
		assertTrue(errorResult.contains("must not be blank"));
	}

	@ParameterizedTest
	@ValueSource(strings = {" space at start", "space at end ", " two  spaces  inside", " three   spaces "})
	public void whenAddRoleTrimFields(final String login) throws Exception {
		final UserEntity user = this.ctu.getUser(this.ctu.getRndEntityPos());
		user.setLogin(login);
		final UserEntity postedUser;
		String postedLogin;
		final MvcResult result;

		//when
		result = this.mockMvc.perform(MockMvcRequestBuilders
						.post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(user))
						.with(csrf()))
				.andReturn();

		// then
		assertEquals(201, result.getResponse().getStatus());
		postedUser = this.ctu.getUserFromJson(new JSONObject(result.getResponse().getContentAsString()));
		postedLogin = login.trim();
		while (postedLogin.contains("  "))
			postedLogin = postedLogin.replace("  ", " ");
		assertEquals(postedLogin, postedUser.getLogin());
	}

	@ParameterizedTest
	@ValueSource(strings = {"0", "150"})
	public void whenAddUserWrongFieldSize(final Integer stringSize) throws Exception {
		// given
		final String fieldValue = "a".repeat(stringSize);
		final UserEntity user = this.ctu.getUser(this.ctu.getRndEntityPos());
		user.setLogin(fieldValue);
		final MvcResult result;

		// when
		result = this.mockMvc.perform(MockMvcRequestBuilders
						.post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(user))
						.with(csrf()))
				.andReturn();

		// then
		assertEquals(400, result.getResponse().getStatus());
		final String errorResult = Objects.requireNonNull(result.getResolvedException()).getMessage();
		assertTrue(errorResult.contains("must be between"));
	}
}
