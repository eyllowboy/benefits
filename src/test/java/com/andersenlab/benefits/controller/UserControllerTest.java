package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.LocationRepository;
import com.andersenlab.benefits.repository.RoleRepository;
import com.andersenlab.benefits.repository.UserRepository;
import com.andersenlab.benefits.service.UserService;
import com.andersenlab.benefits.support.RestResponsePage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.util.Optional;

import static java.lang.Math.random;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private final LocationRepository locationRepository;
    private final ControllerTestUtils ctu;
    private final UserService userService;

    @Autowired
    public UserControllerTest(final MockMvc mockMvc,
                              final ObjectMapper objectMapper,
                              final RoleRepository roleRepository,
                              final UserRepository userRepository,
                              final LocationRepository locationRepository,
                              final ControllerTestUtils ctu,
                              final UserService userService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.ctu = ctu;
        this.userService = userService;
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
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/users?page=0&size=" + rndSize)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();
        // then
        final RestResponsePage<UserEntity> pageResult = this.objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
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
        assertEquals("user with id: " + Long.MAX_VALUE + " was not found in the database",
                nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenAddUserIsSuccess() throws Exception {
        // given
        final RoleEntity role = this.roleRepository.save(new RoleEntity("User", "ROLE_USER"));
        final LocationEntity location = this.locationRepository.save(new LocationEntity("Беларусь", "Минск"));
        final UserEntity user = new UserEntity("user", role, location);
        this.userRepository.save(user);
        final UserEntity PostUser = new UserEntity("PostUser", role, location);
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .param("login", PostUser.getLogin())
                        .param("password", "12345")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(user)))
                .andReturn();
        // then
        assertEquals(201, result.getResponse().getStatus());
        assertEquals(2, this.userRepository.findAll().size());
        this.userService.delete(user.getId());
        this.userService.delete(this.userRepository.findByLogin("PostUser").get().getId());
    }

    @Test
    public void whenAddUserAndLoginIsExists() {
        // given
        final UserEntity user = this.ctu.getUser(this.ctu.getRndEntityPos());
        final RoleEntity role = this.roleRepository.save(new RoleEntity("User", "ROLE_USER"));
        final LocationEntity location = this.locationRepository.save(new LocationEntity("Беларусь", "Минск"));
        user.setRoleEntity(role);
        user.setLocation(location);
        this.userRepository.save(user);
        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .param("login", user.getLogin())
                        .param("password", "12345")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)));

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("User with login: " + user.getLogin() + " already exist in database",
                nestedServletException.getCause().getMessage());
        this.userService.delete(user.getId());
    }

    @Test
    public void whenAddUserAndRoleIsNotExists() {

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .param("login", "user")
                        .param("password", "12345")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)));

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("No suitable role for ordinary users",
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
        assertEquals("user with user login: " + userSetLoginTo.getLogin() + " already exist in database",
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
        assertEquals("role with id: " + Long.MAX_VALUE + " was not found in the database",
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
        assertEquals("user with id: " + Long.MAX_VALUE + " was not found in the database",
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
        assertEquals("User with id: " + Long.MAX_VALUE + " was not found in the database",
                nestedServletException.getCause().getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "    "})
    public void whenAddUserWrongObligatoryFields(final String login) throws Exception {
        //given
        final UserEntity savedUser = this.ctu.getUser(this.ctu.getRndEntityPos());
        final RoleEntity role = this.roleRepository.save(new RoleEntity("User", "ROLE_USER"));
        final LocationEntity location = this.locationRepository.save(new LocationEntity("Беларусь", "Минск"));
        savedUser.setRoleEntity(role);
        savedUser.setLocation(location);
        this.userRepository.save(savedUser);
        final UserEntity user = this.ctu.getUser(this.ctu.getRndEntityPos());
        user.setLogin(login);

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/users")
                    .param("login", login)
                    .param("password", "12345")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(user))
                    .with(csrf()));
        });

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("Adding UserEntity haven't done. Obligatory field 'login' has no data", nestedServletException.getCause().getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {" space at start", "space at end ", " two  spaces  inside", " three   spaces "})
    public void whenAddRoleTrimFields(final String login) throws Exception {
        final UserEntity savedUser = this.ctu.getUser(this.ctu.getRndEntityPos());
        final RoleEntity role = this.roleRepository.save(new RoleEntity("User", "ROLE_USER"));
        final LocationEntity location = this.locationRepository.save(new LocationEntity("Беларусь", "Минск"));
        savedUser.setRoleEntity(role);
        savedUser.setLocation(location);
        this.userRepository.save(savedUser);
        final UserEntity user = this.ctu.getUser(this.ctu.getRndEntityPos());
        user.setLogin(login);
        final UserEntity postedUser;
        String postedLogin;
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .param("login", login)
                        .param("password", "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andReturn();

        // then
        assertEquals(201, result.getResponse().getStatus());
        postedUser = this.ctu.getUserFromJson(new JSONObject(result.getResponse().getContentAsString()));
        postedLogin = login.trim();
        while (postedLogin.contains("  "))
            postedLogin = postedLogin.replace("  ", " ");
        assertEquals(postedLogin, postedUser.getLogin());
        final List<UserEntity> allUsers = this.userRepository.findAll();
        for (UserEntity foundUser : allUsers) {
            this.userService.delete(foundUser.getId());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "150"})
    public void whenAddUserWrongFieldSizeLogin(final Integer stringSize) throws Exception {
        // given
        final UserEntity user = this.ctu.getUser(this.ctu.getRndEntityPos());
        final RoleEntity role = this.roleRepository.save(new RoleEntity("User", "ROLE_USER"));
        final LocationEntity location = this.locationRepository.save(new LocationEntity("Беларусь", "Минск"));
        user.setRoleEntity(role);
        user.setLocation(location);
        this.userRepository.save(user);
        final String fieldValue = "a".repeat(stringSize);

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/users")
                    .param("login", fieldValue)
                    .param("password", "12345")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()));

        });
        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("Incorrect field login data size - must be between 3 and 20", nestedServletException.getCause().getMessage());

    }
}
