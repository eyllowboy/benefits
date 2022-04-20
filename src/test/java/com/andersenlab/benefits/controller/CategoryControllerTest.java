package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.CategoryEntity;
import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.repository.*;
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

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.lang.Math.random;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
            CategoryEntity category = new CategoryEntity("Category" + i);
            this.categoryRepository.save(category);
        }
    }

    @Test
    public void whenAddCategorySuccess() throws Exception {
        //given
        final CategoryEntity category = new CategoryEntity("Разное");
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(category))
                        .with(csrf()))
                .andDo(print())
                // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.title", is(category.getTitle())));
    }

    @Test
    public void whenGetCategoryByIdSuccess() throws Exception {
        //given
        final Optional<CategoryEntity> categoryFromContainer = this.categoryRepository.findByTitle("Category2");
        //when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/categories/{id}", categoryFromContainer.get().getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with((csrf())))
                .andDo(print())
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(categoryFromContainer.get().getId().intValue())))
                .andExpect(jsonPath("$.title", is(categoryFromContainer.get().getTitle())));
    }


    @Test
    public void whenGetSomeSizeCategoriesSuccess() throws Exception {
        // given
        final int rndSize = (int) (random() * (5 - 1) + 1);
        final Page<CategoryEntity> foundCategory = this.categoryRepository.findAll(PageRequest.of(0, rndSize));
        final MvcResult result;
        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/categories?page=0&size=" + rndSize)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andReturn();
        // then
        final RestResponsePage<CategoryEntity> pageResult = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {});
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(foundCategory, pageResult);
    }


    @Test
    public void whenGetCategoryByIdFailIdNotExists() {
        //given
        final CategoryEntity lastCategoryFromContainer = this.categoryRepository.findByTitle("Category5").get();
        // when
        NestedServletException NestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders
                    .get("/categories/{id}", lastCategoryFromContainer.getId() + 1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()));
        });
        // then
        assertEquals(IllegalStateException.class, NestedServletException.getCause().getClass());
        assertEquals("Category with this id was not found in the database", NestedServletException.getCause().getMessage());
    }


    @Test
    public void whenAddCategoryFailCategoryExists() {
        // given
        final CategoryEntity category = new CategoryEntity("Category3");
        // when
        NestedServletException NestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders.post("/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(category))
                    .with(csrf()));
        });
        // then
        assertEquals(IllegalStateException.class, NestedServletException.getCause().getClass());
        assertEquals("Category with title '" + category.getTitle() + "' already exists", NestedServletException.getCause().getMessage());
    }

    @Test
    public void whenUpdateCategorySuccess() throws Exception {
        //given
        final CategoryEntity category = this.categoryRepository.findByTitle("Category5").orElseThrow();
        category.setTitle("UpdatedCategory");

        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/categories/{id}", category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(category))
                        .with((csrf())))
                .andDo(print())
        // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(category.getId().intValue())))
                .andExpect(jsonPath("$.title", is(this.categoryRepository.findById(category.getId()).orElseThrow().getTitle())));
    }

    @Test
    public void whenUpdateCategoryFailIdNotExists() throws Exception {
        // given
        final CategoryEntity lastCategoryFromContainer = this.categoryRepository.findByTitle("Category5").get();
        final Long notExistId = lastCategoryFromContainer.getId() + 1;
        final CategoryEntity category = new CategoryEntity(notExistId, "Прочее");
        final String categoryEntity = (this.objectMapper).writeValueAsString(category);
        // when
        NestedServletException nestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders
                    .patch("/categories/{id}", category.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(categoryEntity)
                    .with(csrf()));
        });
        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("Category with this id was not found in the database", nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenDeleteCategoryWithoutDiscountsSuccess() throws Exception {
        // given
        final Optional<CategoryEntity> LastCategoryFromContainer = this.categoryRepository.findByTitle("Category5");
        Long lastIdEntity = LastCategoryFromContainer.get().getId();
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/categories/{id}", lastIdEntity)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                // then
                .andExpect(status().isOk());
    }

    @Test
    public void whenDeleteCategoryFailHasActiveDiscounts() {
        //given
        final CategoryEntity savedCategory = this.categoryRepository.save(new CategoryEntity("NewCategory"));
        final DiscountEntity discount = this.ctu.getDiscount(5);
        final Set<CategoryEntity> categoryEntitySet = Set.of(savedCategory);
        discount.setCategories(categoryEntitySet);
        this.discountRepository.save(discount);

        // when
        NestedServletException nestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders
                    .delete("/categories/{id}", savedCategory.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()));
        });
        nestedServletException.printStackTrace();
        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("There is active discounts in this Category in database", nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenDeleteCategoryFailIdNotExists() {
        // given
        final CategoryEntity lastCategoryFromContainer = this.categoryRepository.findByTitle("Category5").get();
        final Long lastIdEntity = lastCategoryFromContainer.getId();
        // when
        NestedServletException nestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders
                    .delete("/categories/{id}", lastIdEntity + 1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()));
        });
        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("Category with id: '" + (lastIdEntity + 1) + "' was not found in the database", nestedServletException.getCause().getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "    "})
    public void whenAddCategoryWrongObligatoryFields(final String title) throws Exception {
        // given
        final CategoryEntity category = new CategoryEntity(title);
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(category))
                        .with(csrf()))
                .andReturn();

        // then
        assertEquals(400, result.getResponse().getStatus());
        final String errorResult = Objects.requireNonNull(result.getResolvedException()).getMessage();
        assertTrue(errorResult.contains("must not be blank"));
    }

    @ParameterizedTest
    @ValueSource(strings = {" space at start", "space at end ", " two  spaces  inside", " three   spaces   inside"})
    public void whenAddCategoryTrimFields(final String title) throws Exception {
        // given
        final CategoryEntity category = new CategoryEntity(title);
        final CategoryEntity postedCategory;
        String postedTitle;
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(category))
                        .with(csrf()))
                .andReturn();

        // then
        assertEquals(201, result.getResponse().getStatus());
        postedCategory = this.ctu.getCategoryFromJson(new JSONObject(result.getResponse().getContentAsString()));
        postedTitle = title.trim();
        while (postedTitle.contains("  "))
            postedTitle = postedTitle.replace("  ", " ");
        assertEquals(postedTitle, postedCategory.getTitle());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2", "150"})
    public void whenAddCategoryWrongFieldSize(final Integer stringSize) throws Exception {
        // given
        final String fieldValue = "a".repeat(stringSize);
        final CategoryEntity category = new CategoryEntity(fieldValue);
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(category))
                        .with(csrf()))
                .andReturn();

        // then
        assertEquals(400, result.getResponse().getStatus());
        final String errorResult = Objects.requireNonNull(result.getResolvedException()).getMessage();
        assertTrue(errorResult.contains("must be between"));
    }
}
