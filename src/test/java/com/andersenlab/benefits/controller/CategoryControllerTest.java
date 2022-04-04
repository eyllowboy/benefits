package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.CategoryEntity;
import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.domain.DiscountType;
import com.andersenlab.benefits.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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

    private final LocationRepository locationRepository;

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    private final DiscountRepository discountRepository;

    private final CompanyRepository companyRepository;

    private final CategoryRepository categoryRepository;

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer =
            (new PostgreSQLContainer<>("postgres"))
                    .withDatabaseName("benefits")
                    .withUsername("benefits")
                    .withPassword("ben0147");

    @Autowired
    public CategoryControllerTest(MockMvc mockMvc, LocationRepository locationRepository, RoleRepository roleRepository, UserRepository userRepository, DiscountRepository discountRepository, CompanyRepository companyRepository, CategoryRepository categoryRepository) {
        this.mockMvc = mockMvc;
        this.locationRepository = locationRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.discountRepository = discountRepository;
        this.companyRepository = companyRepository;
        this.categoryRepository = categoryRepository;
    }


    @DynamicPropertySource
    public static void postgreSQLProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    }

    @BeforeEach
    private void deleteAndSaveCategoryInContainer() {
        this.discountRepository.deleteAll();
        this.categoryRepository.deleteAll();
        this.companyRepository.deleteAll();
        this.userRepository.deleteAll();
        this.locationRepository.deleteAll();
        this.roleRepository.deleteAll();
        createAndSaveCategoryInContainer();
    }

    private void createAndSaveCategoryInContainer() {
        final int size = 5;
        for (long i = 1; i <= size; i++) {
            CategoryEntity category = new CategoryEntity("Category" + i);
            categoryRepository.save(category);
        }
    }

    @Test
    public void whenAddCategorySuccess() throws Exception {
        //given
        final String title = "Разное";
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("title", title)
                        .with(csrf()))
                .andDo(print())
                // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.title", is(title)));
    }

    @Test
    public void whenGetCategoryByIdSuccess() throws Exception {
        //given
        final Optional<CategoryEntity> categoryFromContainer = categoryRepository.findByTitle("Category2");
        //when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/categories/{id}", categoryFromContainer.get().getId().longValue())
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
    public void whenGetAllCategoriesSuccess() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }


    @Test
    public void whenGetCategoryByIdFailIdNotExists() throws Exception {
        //given
        final CategoryEntity lastCategoryFromContainer = categoryRepository.findByTitle("Category5").get();
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
    public void whenAddCategoryFailCategoryExists() throws Exception {
        // given
        final String title = "Category3";
        // when
        NestedServletException NestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders.post("/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("title", title)
                    .with(csrf()));
        });
        // then
        assertEquals(IllegalStateException.class, NestedServletException.getCause().getClass());
        assertEquals("Category with title '" + title + "' already exists", NestedServletException.getCause().getMessage());
    }

    @Test
    public void whenUpdateCategorySuccess() throws Exception {
        //given
        final Optional<CategoryEntity> lastCategoryFromContainer = categoryRepository.findByTitle("Category5");
        final CategoryEntity category = lastCategoryFromContainer.get();
        category.setTitle("UpdatedCategory");
        categoryRepository.save(category);
        final String categoryEntity = (new ObjectMapper()).writeValueAsString(category);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .put("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryEntity)
                        .with((csrf())))
                .andDo(print())
                // then
                .andExpect(status().isOk());

    }

    @Test
    public void whenUpdateCategoryFailIdNotExists() throws Exception {
        // given
        final CategoryEntity lastCategoryFromContainer = categoryRepository.findByTitle("Category5").get();
        final Long notExistId = lastCategoryFromContainer.getId() + 1;
        final CategoryEntity category = new CategoryEntity(notExistId, "Прочее");
        final String categoryEntity = (new ObjectMapper()).writeValueAsString(category);
        // when
        NestedServletException nestedServletException = assertThrows(NestedServletException.class, () -> {
            this.mockMvc.perform(MockMvcRequestBuilders.put("/categories")
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
        final Optional<CategoryEntity> LastCategoryFromContainer = categoryRepository.findByTitle("Category5");
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
    public void whenDeleteCategoryFailHasActiveDiscounts() throws Exception {
        //given
        final CategoryEntity categoryWithActiveDiscount = new CategoryEntity("NewCategory");
        final DiscountEntity discount = new DiscountEntity();
        discount.setType("type");
        discount.setDescription("descriptioin");
        discount.setDiscount_condition("discount_condition");
        discount.setSizeDiscount("10");
        discount.setDiscount_type(DiscountType.DISCOUNT);
        discount.setDateBegin(new Date());
        discount.setDateFinish(new Date());
        discount.setImageDiscount("imageDiscont");

        var savedCategory = categoryRepository.save(categoryWithActiveDiscount);
        final Set<CategoryEntity> categoryEntitySet = Set.of(savedCategory);
        discount.setCategories(categoryEntitySet);
        discountRepository.save(discount);
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
    public void whenDeleteCategoryFailIdNotExists() throws Exception {
        // given
        final CategoryEntity lastCategoryFromContainer = categoryRepository.findByTitle("Category5").get();
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
}
