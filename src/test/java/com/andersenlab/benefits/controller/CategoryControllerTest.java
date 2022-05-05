package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.CategoryEntity;
import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.repository.CategoryRepository;
import com.andersenlab.benefits.repository.DiscountRepository;
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

import java.util.Optional;
import java.util.Set;

import static java.lang.Math.random;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@WithMockUser

public class CategoryControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DiscountRepository discountRepository;
    private final CategoryRepository categoryRepository;
    private final ControllerTestUtils ctu;

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer =
            (new PostgreSQLContainer<>("postgres"))
                    .withDatabaseName("benefits")
                    .withUsername("benefits")
                    .withPassword("ben0147");

    @Autowired
    public CategoryControllerTest(final MockMvc mockMvc,
                                  final DiscountRepository discountRepository,
                                  final CategoryRepository categoryRepository,
                                  final ControllerTestUtils ctu,
                                  final ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.discountRepository = discountRepository;
        this.categoryRepository = categoryRepository;
        this.ctu = ctu;
        this.objectMapper = objectMapper;
    }


    @DynamicPropertySource
    public static void postgreSQLProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    }

    @BeforeEach
    private void deleteAndSaveCategoryInContainer() {
        this.ctu.clearTables();
        createAndSaveCategoryInContainer();
    }

    private void createAndSaveCategoryInContainer() {
        final int size = 5;
        for (long i = 1; i <= size; i++) {
            final CategoryEntity category = new CategoryEntity("Category" + i);
            this.categoryRepository.save(category);
        }
    }

    @Test
    public void whenAddCategorySuccess() throws Exception {
        //given
        final CategoryEntity categoryBeforeOperation = new CategoryEntity("Разное");
        final int sizeBeforeAdd = this.categoryRepository.findAll().size();
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(categoryBeforeOperation))
                        .with(csrf()))
                .andReturn();

        // then
        final CategoryEntity categoryAfterOperation = this.ctu.getCategoryFromJson(new JSONObject(result.getResponse().getContentAsString()));
        assertEquals(201, result.getResponse().getStatus());
        assertEquals(sizeBeforeAdd + 1, this.categoryRepository.findAll().size());
        assertEquals(categoryBeforeOperation.getTitle(), categoryAfterOperation.getTitle());

    }

    @Test
    public void whenGetCategoryByIdSuccess() throws Exception {
        //given
        final CategoryEntity categoryBeforeOperation = this.categoryRepository.findByTitle("Category2").orElseThrow();
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/categories/{id}", categoryBeforeOperation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with((csrf())))
                .andReturn();

        // then
        final CategoryEntity categoryAfterOperation = this.ctu.getCategoryFromJson(new JSONObject(result.getResponse().getContentAsString()));
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(categoryBeforeOperation, categoryAfterOperation);
    }


    @Test
    public void whenGetSomeSizeCategoriesSuccess() throws Exception {
        // given
        final int rndSize = (int) (random() * (5 - 1) + 1);
        final Page<CategoryEntity> categoriesBeforeOperation = this.categoryRepository.findAll(PageRequest.of(0, rndSize));
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/categories?page=0&size=" + rndSize)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        final RestResponsePage<CategoryEntity> categoriesAfterOperation = this.objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(categoriesBeforeOperation, categoriesAfterOperation);
    }


    @Test
    public void whenGetCategoryByIdFailIdNotExists() {
        // when
        final NestedServletException NestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders
                    .get("/categories/{id}", Long.MAX_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()));
        });

        // then
        assertEquals(IllegalStateException.class, NestedServletException.getCause().getClass());
        assertEquals(Optional.empty(), this.categoryRepository.findById(Long.MAX_VALUE));
        assertEquals("category with id: " + Long.MAX_VALUE + " was not found in the database", NestedServletException.getCause().getMessage());
    }


    @Test
    public void whenAddCategoryTheSameTitleAlreadyExistFail() {
        // given
        final CategoryEntity categoryBeforeOperation = new CategoryEntity("Category3");

        // when
        final NestedServletException NestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(categoryBeforeOperation))
                    .with(csrf()));
        });

        // then
        assertEquals(IllegalStateException.class, NestedServletException.getCause().getClass());
        assertEquals(categoryBeforeOperation.getTitle(), this.categoryRepository.
                findByTitle(categoryBeforeOperation.getTitle()).orElseThrow().getTitle());
        assertEquals("category with category title: Category3 already exist in database",
                NestedServletException.getCause().getMessage());

    }

    @Test
    public void whenUpdateCategorySuccess() throws Exception {
        //given
        final CategoryEntity categoryBeforeOperation = this.categoryRepository.findByTitle("Category5").orElseThrow();
        categoryBeforeOperation.setTitle("UpdatedCategory");
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/categories/{id}", categoryBeforeOperation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(categoryBeforeOperation))
                        .with((csrf())))
                .andReturn();

        // then
        final CategoryEntity categoryAfterOperation = this.ctu.getCategoryFromJson(new JSONObject(result.getResponse().getContentAsString()));
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(categoryBeforeOperation, categoryAfterOperation);
    }

    @Test
    public void whenUpdateCategoryFailIdNotExists() throws Exception {
        // given
        final CategoryEntity categoryBeforeOperation = new CategoryEntity(Long.MAX_VALUE, "Прочее");

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders
                    .patch("/categories/{id}", Long.MAX_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(categoryBeforeOperation))
                    .with(csrf()));
        });

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals(Optional.empty(), this.categoryRepository.findById(Long.MAX_VALUE));
        assertEquals("category with id: " + categoryBeforeOperation.getId() + " was not found in the database", nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenDeleteCategoryWithoutDiscountsSuccess() throws Exception {
        // given
        final Optional<CategoryEntity> categoryBeforeOperation = this.categoryRepository.findByTitle("Category5");
        final int sizeBeforeDelete = this.categoryRepository.findAll().size();

        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/categories/{id}", categoryBeforeOperation.orElseThrow().getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())

                // then
                .andExpect(status().isOk());
        assertEquals(Optional.empty(), this.categoryRepository.findById(categoryBeforeOperation.orElseThrow().getId()));
        assertEquals(sizeBeforeDelete - 1, this.categoryRepository.findAll().size());
    }

    @Test
    public void whenDeleteCategoryFailHasActiveDiscounts() {
        //given
        final CategoryEntity categoryBeforeOperation = this.categoryRepository.save(new CategoryEntity("NewCategory"));
        final DiscountEntity discount = this.ctu.getDiscount(5);
        final Set<CategoryEntity> categoryEntitySet = Set.of(categoryBeforeOperation);
        discount.setCategories(categoryEntitySet);
        this.discountRepository.save(discount);

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders
                    .delete("/categories/{id}", categoryBeforeOperation.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()));
        });

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("There is active category in this discount in database", nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenDeleteCategoryFailIdNotExists() {
        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders
                    .delete("/categories/{id}", Long.MAX_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()));
        });

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals(Optional.empty(), this.categoryRepository.findById(Long.MAX_VALUE));
        assertEquals("category with id: " + Long.MAX_VALUE + " was not found in the database", nestedServletException.getCause().getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "    "})
    public void whenAddCategoryWrongObligatoryFields(final String title) {
        // given
        final CategoryEntity categoryBeforeOperation = new CategoryEntity(title);

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(categoryBeforeOperation))
                    .with(csrf()));
        });

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("Adding CategoryEntity haven't done. Obligatory field 'title' has no data", nestedServletException.getCause().getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {" space at start", "space at end ", " two  spaces  inside", " three   spaces"})
    public void whenAddCategoryTrimFields(final String title) throws Exception {
        // given
        final CategoryEntity categoryBeforeOperation = new CategoryEntity(title);
        final CategoryEntity categoryAfterOperation;
        String postedTitle;
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(categoryBeforeOperation))
                        .with(csrf()))
                .andReturn();

        // then
        assertEquals(201, result.getResponse().getStatus());
        categoryAfterOperation = this.ctu.getCategoryFromJson(new JSONObject(result.getResponse().getContentAsString()));
        postedTitle = title.trim();
        while (postedTitle.contains("  "))
            postedTitle = postedTitle.replace("  ", " ");
        assertEquals(postedTitle, categoryAfterOperation.getTitle());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2", "150"})
    public void whenAddCategoryWrongFieldSize(final Integer stringSize) throws Exception {
        // given
        final String fieldValue = "a".repeat(stringSize);
        final CategoryEntity categoryBeforeOperation = new CategoryEntity(fieldValue);

        // when
        final NestedServletException exception = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(categoryBeforeOperation))
                        .with(csrf())));

        // then
        assertEquals(IllegalStateException.class, exception.getCause().getClass());
        assertTrue(exception.getMessage().contains("Incorrect field title data size - must be between 3 and 20"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2", "150"})
    public void whenUpdateCategoryWrongFieldSize(final Integer stringSize) {
        // given
        final CategoryEntity categoryBeforeOperation = this.categoryRepository.save(this.ctu.getCategoryList().iterator().next());
        categoryBeforeOperation.setTitle("a".repeat(stringSize));

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/categories/{id}", categoryBeforeOperation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(categoryBeforeOperation))
                        .with(csrf())));

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertTrue(nestedServletException.getCause().getMessage().contains("Incorrect field title data size - must be between 3 and 20"));
    }
}
