package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.*;
import com.andersenlab.benefits.repository.CategoryRepository;
import com.andersenlab.benefits.repository.CompanyRepository;
import com.andersenlab.benefits.repository.LocationRepository;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.NestedServletException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.Set;

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

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CompanyRepository companyRepository;

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
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    @Order(2)
    public void whenGetDiscountPositiveScenario() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.type", is("type1")));
    }

    @Test
    @Order(3)
    public void whenGetDiscountWithIncorrectId() throws Exception {
        // given
        final long id = 8L;
        // when
        NestedServletException NestedServletException = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/discounts/{id}", id)));
        // then
        assertEquals(IllegalStateException.class,
                NestedServletException.getCause().getClass());
        assertEquals("The discount with id: " + id + " was not found in the database",
                NestedServletException.getCause().getMessage());
    }

    @Test
    @Order(4)
    public void whenAddDiscountPositiveScenario() throws Exception {
        // given
        final Set<CategoryEntity> categories = new HashSet<>();
        categories.add(categoryRepository.findById(1L).get());
        final Set<LocationEntity> locations = new HashSet<>();
        locations.add(locationRepository.findById(1L).get());
        final CompanyEntity company = companyRepository.findById(3L).get();
        final DiscountEntity discount = new DiscountEntity(6L, "type02", "desc02", "cond", "20", DiscountType.DISCOUNT, valueOf("2022-01-20 15:34:23"), valueOf("2022-02-20 15:34:23"), "image", locations, categories, company);
        // when
        this.mockMvc.perform(
                        post("/discounts")
                                .content(objectMapper.writeValueAsString(discount))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", isA(Number.class)))
                .andExpect(jsonPath("$.type", is("type02")));
    }

    @Test
    @Order(5)
    public void whenUpdatePositiveScenario() throws Exception {
        // given
        final Set<CategoryEntity> categories = new HashSet<>();
        categories.add(categoryRepository.findById(1L).get());
        final Set<LocationEntity> locations = new HashSet<>();
        locations.add(locationRepository.findById(1L).get());
        final CompanyEntity company = companyRepository.findById(3L).get();
        final DiscountEntity discount = new DiscountEntity(2L, "type01", "desc", "cond", "20", DiscountType.DISCOUNT, valueOf("2022-01-20 15:34:23"), valueOf("2022-02-20 15:34:23"), "image", locations, categories, company);
        final String discountEntity = new ObjectMapper().writeValueAsString(discount);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .put("/discounts/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(discountEntity))
                .andDo(print())
                // then
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    public void whenUpdateNegativeScenario() throws JsonProcessingException {
        // given
        final Set<CategoryEntity> categories = new HashSet<>();
        categories.add(categoryRepository.findById(1L).get());
        final Set<LocationEntity> locations = new HashSet<>();
        locations.add(locationRepository.findById(1L).get());
        final CompanyEntity company = companyRepository.findById(3L).get();
        final DiscountEntity discount = new DiscountEntity(8L, "type", "desc", "cond", "20", DiscountType.DISCOUNT, valueOf("2022-01-20 15:34:23"), valueOf("2022-02-20 15:34:23"), "image", locations, categories, company);
        final String discountEntity = new ObjectMapper().writeValueAsString(discount);
        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                mockMvc.perform(MockMvcRequestBuilders
                        .put("/discounts/{id}", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(discountEntity)));
        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("The discount with id: " + Long.MAX_VALUE + " was not found in the database", nestedServletException.getCause().getMessage());
    }

    @Test
    @Order(7)
    public void whenDeletePositiveScenario() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/discounts/4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                // then
                .andExpect(status().isOk());
    }


    @Test
    @Order(8)
    public void whenDeleteNegativeScenario() {
        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/discounts/{id}", Long.MAX_VALUE)));
        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("The discount with id: " + Long.MAX_VALUE + " was not found in the database",
                nestedServletException.getCause().getMessage());
    }

    @Test
    void whenFindByCityAndCategoryAndDateIsPositiveScenario() throws Exception {
        //given
        final LinkedMultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("city", "Москва");
        requestParams.add("category", "Еда");
        //when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .params(requestParams))
                //then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type", is("type1")))
                .andReturn();

    }

    @Test
    void whenFindByTypeIsPositiveScenario() throws Exception {
        //when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("type", "type1"))
                //then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type", is("type1")));
    }

    @Test
    void whenFindBySizeDiscountIsPositiveScenario() throws Exception {
        //when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/size")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("size", "10"))
                //then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sizeDiscount", is("10")));
    }
}


