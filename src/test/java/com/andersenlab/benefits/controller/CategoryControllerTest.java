package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.CategoryEntity;
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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer =
            (new PostgreSQLContainer<>("postgres"))
                    .withDatabaseName("benefits")
                    .withUsername("benefits")
                    .withPassword("ben0147");

    @DynamicPropertySource
    public static void postgreSQLProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    }

    @Test
    public void whenGetAllCategoriesSuccess() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    public void whenGetCategoryByIdSuccess() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/categories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Еда")));
    }

    @Test
    public void whenGetCategoryByIdFailIdNotExists() throws Exception {
        // when
        NestedServletException NestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders
                    .get("/categories/{id}", 100L)
                    .contentType(MediaType.APPLICATION_JSON));
        });
        // then
        assertEquals(IllegalStateException.class, NestedServletException.getCause().getClass());
        assertEquals("Category with this id was not found in the database", NestedServletException.getCause().getMessage());
    }

    @Test
    public void whenAddCategorySuccess() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("title", "Разное"))
                .andDo(print())
                // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.title", is("Разное")));
    }

    @Test
    public void whenAddCategoryFailCategoryExists() throws Exception {
        // given
        final String title = "Обучение";
        // when
        NestedServletException NestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders.post("/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("title", "Обучение"));
        });
        // then
        assertEquals(IllegalStateException.class, NestedServletException.getCause().getClass());
        assertEquals("Category with title '" + title + "' already exists", NestedServletException.getCause().getMessage());
    }

    @Test
    public void whenUpdateCategorySuccess() throws Exception {
        //given
        final CategoryEntity category = new CategoryEntity(6L, "Food");
        final String categoryEntity = (new ObjectMapper()).writeValueAsString(category);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .put("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryEntity))
                .andDo(print())
                // then
                .andExpect(status().isOk());
    }

    @Test
    public void whenUpdateCategoryFailIdNotExists() throws Exception {
        // given
        final CategoryEntity category = new CategoryEntity(100L, "Прочее");
        final String categoryEntity = (new ObjectMapper()).writeValueAsString(category);
        // when
        NestedServletException nestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders.put("/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(categoryEntity));
        });
        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("Category with this id was not found in the database", nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenDeleteCategorySuccess() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/categories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                // then
                .andExpect(status().isOk());
    }

    @Test
    public void whenDeleteCategoryFailIdNotExists() throws Exception {
        // given
        final Long id = 100L;
        // when
        NestedServletException nestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders
                    .delete("/categories/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON));
        });
        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("Category with id: '" + id + "' was not found in the database", nestedServletException.getCause().getMessage());
    }
}
