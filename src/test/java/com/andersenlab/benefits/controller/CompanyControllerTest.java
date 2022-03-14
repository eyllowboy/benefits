package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.CompanyEntity;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@WithMockUser
class CompanyControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

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
    void whenAddCompanyWithPositiveScenario() throws Exception {

        final CompanyEntity company = new CompanyEntity(6L, "company6", "description6", "address6", "8900-00-00", "link06");

        this.mockMvc.perform(
                        post("/companies")
                                .content(objectMapper.writeValueAsString(company))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", isA(Number.class)))
                .andExpect(jsonPath("$.title", is("company6")));
    }

    @Test
    void whenGetCompanyByIdIsOk() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/companies/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("company1")));
    }

    @Test
    void whenGetAllCompanyIsOk() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void whenGetCompanyWithIncorrectId() throws Exception {
        final long id = 8L;
        NestedServletException NestedServletException = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/companies/{id}", id).with(csrf())));
        assertEquals(IllegalStateException.class,
                NestedServletException.getCause().getClass());

        assertEquals("Company with this id was not found in the database.",
                NestedServletException.getCause().getMessage());
    }

    @Test
    void whenUpdatedCompanyPositiveScenario() throws Exception {

        final CompanyEntity company = new CompanyEntity(2L, "company22", "description22", "address22", "8911-11-11", "link22");

        final String companyEntity = new ObjectMapper().writeValueAsString(company);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .put("/companies/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(companyEntity)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void whenUpdatedCompanyNegativeScenario() throws Exception {

        final long id = 9L;
        final CompanyEntity company = new CompanyEntity(id, "company9", "description9", "address9", "8911-11-11", "link9");

        final String companyEntity = new ObjectMapper().writeValueAsString(company);

        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(MockMvcRequestBuilders
                        .put("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(companyEntity)
                        .with(csrf())));
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("The company with id: " + id + " was not found in the database.", nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenDeletePositiveScenario() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/companies/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void whenDeleteNegativeScenario() {
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/companies/{id}", Long.MAX_VALUE)
                        .with(csrf())));
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("The company with id: " + Long.MAX_VALUE + " was not found in the database.",
                nestedServletException.getCause().getMessage());
    }
}