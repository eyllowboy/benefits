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
public class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    public void whenGetAllRoles() throws Exception {
        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    public void whenGetRoleByIdAndIdExists() throws Exception {
        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/roles/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("System administrator")))
                .andExpect(jsonPath("$.code", is("ROLE_ADMIN")));
    }

    @Test
    public void whenGetRoleByIdAndIdNotExists() {
        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/roles/{id}", Long.MAX_VALUE)));

        // then
        assertEquals(IllegalStateException.class,
                nestedServletException.getCause().getClass());
        assertEquals("Role with this id was not found in the database",
                nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenAddRoleIsSuccess() throws Exception {
        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("name", "new_name_1")
                        .param("code", "new_role_code_1"))
                .andDo(print())
                // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", isA(Number.class)))
                .andExpect(jsonPath("$.name", is("new_name_1")))
                .andExpect(jsonPath("$.code", is("new_role_code_1")));
    }

    @Test
    public void whenAddRoleAndCodeIsExists() {
        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(MockMvcRequestBuilders
                        .post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("name", "new_name_2")
                        .param("code", "ROLE_ADMIN")));

        // then
        assertEquals(IllegalStateException.class,
                nestedServletException.getCause().getClass());
        assertEquals("Role with such 'code' is already exists",
                nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenUpdateRoleWithNewNameAndCodeIsSuccess() throws Exception {
        // given
        final RoleEntity roleEntity = new RoleEntity(5L, "name", "new_role_code_2");
        final String roleEntityAsJsonString = new ObjectMapper().writeValueAsString(roleEntity);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleEntityAsJsonString))
                .andDo(print())
                // then
                .andExpect(status().isOk());
    }

    @Test
    public void whenUpdateRoleWithNewNameIsSuccess() throws Exception {
        // given
        final RoleEntity roleEntity = new RoleEntity(2L, "name", "ROLE_MODERATOR");
        final String roleEntityAsJsonString = new ObjectMapper().writeValueAsString(roleEntity);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleEntityAsJsonString))
                .andDo(print())
                // then
                .andExpect(status().isOk());
    }

    @Test
    public void whenUpdateRoleAndCodeIsExists() throws Exception {
        // given
        final RoleEntity roleEntity = new RoleEntity(1L, "name", "ROLE_USER");
        final String roleEntityAsJsonString = new ObjectMapper().writeValueAsString(roleEntity);

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(MockMvcRequestBuilders
                        .put("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleEntityAsJsonString)));

        // then
        assertEquals(IllegalStateException.class,
                nestedServletException.getCause().getClass());
        assertEquals("Role with such 'code' is already exists",
                nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenUpdateRoleAndIdNotExists() throws Exception {
        // given
        final RoleEntity roleEntity = new RoleEntity(Long.MAX_VALUE, "name", "code");
        final String roleEntityAsJsonString = new ObjectMapper().writeValueAsString(roleEntity);

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(MockMvcRequestBuilders
                        .put("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleEntityAsJsonString)));

        // then
        assertEquals(IllegalStateException.class,
                nestedServletException.getCause().getClass());
        assertEquals("Role with this id was not found in the database",
                nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenDeleteRoleIsSuccess() throws Exception {
        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/roles/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                // then
                .andExpect(status().isOk());
    }

    @Test
    public void whenDeleteRoleAndIdNotExists() {
        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(MockMvcRequestBuilders
                        .delete("/roles/{id}", Long.MAX_VALUE)));

        // then
        assertEquals(IllegalStateException.class,
                nestedServletException.getCause().getClass());
        assertEquals("Role with this id was not found in the database",
                nestedServletException.getCause().getMessage());
    }
}