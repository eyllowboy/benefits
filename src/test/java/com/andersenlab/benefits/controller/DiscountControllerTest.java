package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.Discount;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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

import static java.sql.Timestamp.valueOf;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DiscountControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("benefits")
            .withUsername("benefits")
            .withPassword("ben0147");


    @DynamicPropertySource
    public static void postgreSQLProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    }


    @Test
    @Order(1)
    public void whenGetAllDiscountsIsOk() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    @Order(2)
    public void whenGetDiscountPositiveScenario() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discount/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("title1")));
    }

    @Test
    @Order(3)
    public void whenGetDiscountWithIncorrectId() throws Exception {
        final long id = 8L;
        NestedServletException NestedServletException = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/discount/{id}", id)));
        assertEquals(IllegalStateException.class,
                NestedServletException.getCause().getClass());

        assertEquals("The discount with id: " + id + " was not found in the database",
                NestedServletException.getCause().getMessage());
    }

    @Test
    @Order(4)
    public void whenAddDiscountPositiveScenario() throws Exception {

        final Discount discount = new Discount(6L, 2L, 3L, "title6", "description", 20, valueOf("2022-01-20 15:34:23"), valueOf("2022-01-20 15:34:23"), 1L, "image");

        this.mockMvc.perform(
                        post("/discount")
                                .content(objectMapper.writeValueAsString(discount))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", isA(Number.class)))
                .andExpect(jsonPath("$.title", is("title6")));
    }

    @Test
    @Order(5)
    public void whenUpdatePositiveScenario() throws Exception {

        final Discount discount = new Discount(2L, 3L, 3L, "title4", "description4", 20, valueOf("2022-01-20 15:34:23"), valueOf("2022-01-20 15:34:23"), 3L, "image3");

        final String discountEntity = new ObjectMapper().writeValueAsString(discount);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .put("/discount/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(discountEntity))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @Order(6)
    public void whenUpdateNegativeScenario() throws JsonProcessingException {

        final Discount discount = new Discount(Long.MAX_VALUE, 2L, 2L, "title1", "description1", 15, valueOf("2022-01-20 15:34:23"), valueOf("2022-01-20 15:34:23"), 2L, "image2");

        final String discountEntity = new ObjectMapper().writeValueAsString(discount);

        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(MockMvcRequestBuilders
                        .put("/discount/{id}", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(discountEntity)));
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("The discount with id: " + Long.MAX_VALUE + " was not found in the database", nestedServletException.getCause().getMessage());
    }

    @Test
    @Order(7)
    public void whenDeletePositiveScenario() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/discount/4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }


    @Test
    @Order(8)
    public void whenDeleteNegativeScenario() {
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/discount/{id}", Long.MAX_VALUE)));
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("The discount with id: " + Long.MAX_VALUE + " was not found in the database",
                nestedServletException.getCause().getMessage());
    }
}


