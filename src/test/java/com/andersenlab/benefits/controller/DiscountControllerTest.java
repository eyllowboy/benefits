package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.*;
import com.andersenlab.benefits.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WithMockUser
public class DiscountControllerTest {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DiscountRepository discountRepository;
    private final ControllerTestUtils ctu;

    @Autowired
    public DiscountControllerTest(final MockMvc mockMvc,
                                  final ObjectMapper objectMapper,
                                  final DiscountRepository discountRepository,
                                  final ControllerTestUtils ctu) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.discountRepository = discountRepository;
        this.ctu = ctu;
    }

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("benefits")
            .withUsername("benefits")
            .withPassword("ben0147");

    @DynamicPropertySource
    public static void postgreSQLProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    }

    @BeforeEach
    public void clearData() {
        ctu.clearTables();
    }

    @Test
    @Order(1)
    public void whenGetAllDiscountsSuccess() throws Exception {
        // given
        final List<DiscountEntity> discounts = this.discountRepository.saveAll(ctu.getDiscountList());
        final MvcResult result;
        final List<DiscountEntity> discountsResult;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        discountsResult = ctu.getDiscountsFromJson(result.getResponse().getContentAsString());
        discountsResult.forEach(item -> assertTrue(discounts.contains(item)));
    }

    @Test
    @Order(2)
    public void whenGetDiscountByIdSuccess() throws Exception {
        // given
        final int discountPos = ctu.getRndEntityPos();
        final List<DiscountEntity> discounts = this.discountRepository.saveAll(ctu.getDiscountList());
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/{id}", discounts.get(discountPos).getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();
        // then
        assertEquals(200, result.getResponse().getStatus());
        assertTrue(ctu.isDiscountsEquals(
                discounts.get(discountPos),
                ctu.getDiscountFromJson(new JSONObject(result.getResponse().getContentAsString()))));
    }

    @Test
    @Order(3)
    public void whenGetDiscountByIdFailNotExists() {
        // given
        final long id = this.ctu.getRndEntityPos();

        // when
        NestedServletException NestedServletException = assertThrows(NestedServletException.class,
                () -> this.mockMvc.perform(get("/discounts/{id}", id).with(csrf())));

        // then
        assertEquals(IllegalStateException.class, NestedServletException.getCause().getClass());
        assertEquals("The discount with id: " + id + " was not found in the database",
                NestedServletException.getCause().getMessage());
    }

    @Test
    @Order(4)
    public void whenAddDiscountSuccess() throws Exception {
        // given
        final DiscountEntity discount = this.ctu.getDiscount(this.ctu.getRndEntityPos());
        final MvcResult result;

        // when
        result = this.mockMvc.perform(
                        post("/discounts")
                        .with(csrf())
                        .content(this.objectMapper.writeValueAsString(discount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(201, result.getResponse().getStatus());
        assertEquals(1, this.discountRepository.findAll().size());
        assertTrue(this.ctu.isDiscountsEquals(
                discount,
                this.ctu.getDiscountFromJson(new JSONObject(result.getResponse().getContentAsString()))));
    }

    @Test
    @Order(5)
    public void whenAddDiscountFailExists() {
        // given
        final DiscountEntity discount = this.discountRepository.save(this.ctu.getDiscount(this.ctu.getRndEntityPos()));

        // when
        NestedServletException NestedServletException = assertThrows(NestedServletException.class,
                () -> this.mockMvc.perform(
                        post("/discounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(discount))));

        // then
        assertEquals(IllegalStateException.class, NestedServletException.getCause().getClass());
        assertEquals("The discount with id: " + discount.getId() + " already saved in the database",
                NestedServletException.getCause().getMessage());
    }

    @Test
    @Order(6)
    public void whenUpdateDiscountSuccess() throws Exception {
        // given
        final DiscountEntity discount = this.discountRepository.save(this.ctu.getDiscount(this.ctu.getRndEntityPos()));
        discount.setDescription("New Discount Description");
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .put("/discounts/{id}", discount.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(discount)))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        assertTrue(this.ctu.isDiscountsEquals(
                discount,
                this.discountRepository.findById(discount.getId()).orElseThrow()));
    }

    @Test
    @Order(7)
    public void whenUpdateDiscountFailIdNotExists() {
        // given
        final DiscountEntity discount = this.discountRepository.save(this.ctu.getDiscount(this.ctu.getRndEntityPos()));
        discount.setDescription("New Discount Description");
        discount.setId(discount.getId() + 1);

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                        .put("/discounts/{id}", discount.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(discount))));

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("The discount with id: " + discount.getId() + " was not found in the database",
                nestedServletException.getCause().getMessage());
    }

    @Test
    @Order(8)
    public void whenDeleteDiscountSuccess() throws Exception {
        // given
        final List<DiscountEntity> discounts = this.discountRepository.saveAll(this.ctu.getDiscountList());
        final DiscountEntity discount = discounts.get(this.ctu.getRndEntityPos());
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/discounts/{id}", discount.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(discounts.size() - 1, this.discountRepository.findAll().size());
        assertEquals(Optional.empty(), this.discountRepository.findById(discount.getId()));
    }


    @Test
    @Order(9)
    public void whenDeleteDiscountFailIdNotExists() {
        // given
        final List<DiscountEntity> discounts = this.discountRepository.saveAll(this.ctu.getDiscountList());
        final DiscountEntity lastDiscount = discounts.get(discounts.size() - 1);

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> this.mockMvc.perform(MockMvcRequestBuilders
                        // then
                        .delete("/discounts/{id}", lastDiscount.getId() + 1)
                        .with(csrf())));

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("The discount with id: " + (lastDiscount.getId() + 1) + " was not found in the database",
                nestedServletException.getCause().getMessage());
    }

    @Test
    @Order(10)
    void whenFindByCityAndDateSuccess() throws Exception {
        // given
        final List<DiscountEntity> discounts = this.discountRepository.saveAll(this.ctu.getDiscountList());
        final DiscountEntity discount = discounts.get(this.ctu.getRndEntityPos());
        final String city = (discount.getArea().stream().findFirst().orElseThrow()).getCity();
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/filter-by-city")
                        .param("city", city)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        this.ctu.getDiscountsFromJson(result.getResponse().getContentAsString()).forEach(item ->
                assertTrue(item.getArea().stream().anyMatch(areaCity ->
                        areaCity.getCity().equals(city))));
    }

    @Test
    @Order(11)
    void whenFindByCategoryAndDateSuccess() throws Exception {
        // given
        final List<DiscountEntity> discounts = this.discountRepository.saveAll(this.ctu.getDiscountList());
        final DiscountEntity discount = discounts.get(this.ctu.getRndEntityPos());
        final String category = (discount.getCategories().stream().findFirst().orElseThrow()).getTitle();
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/filter-by-category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("category", category)
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        this.ctu.getDiscountsFromJson(result.getResponse().getContentAsString()).forEach(item ->
                assertTrue(item.getCategories().stream().anyMatch(areaCategory ->
                        areaCategory.getTitle().equals(category))));
    }

    @Test
    @Order(12)
    void whenFindByTypeAndDateSuccess() throws Exception {
        // given
        final List<DiscountEntity> discounts = this.discountRepository.saveAll(this.ctu.getDiscountList());
        final DiscountEntity discount = discounts.get(this.ctu.getRndEntityPos());
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/filter-by-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("type", discount.getType())
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        this.ctu.getDiscountsFromJson(result.getResponse().getContentAsString()).forEach(item ->
                assertEquals(item.getType(), discount.getType()));
    }

    @Test
    @Order(13)
    void whenFindBySizeDiscountSuccess() throws Exception {
        // given
        final List<DiscountEntity> discounts = this.discountRepository.saveAll(this.ctu.getDiscountList());
        final DiscountEntity discount = discounts.get(this.ctu.getRndEntityPos());
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/filter-by-size")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("size", discount.getSizeDiscount())
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        this.ctu.getDiscountsFromJson(result.getResponse().getContentAsString()).forEach(item ->
                assertEquals(item.getSizeDiscount(), discount.getSizeDiscount()));
    }

    @Test
    @Order(14)
    void whenFindByCityAndDateEmptyResponse() throws Exception {
        // given
        this.discountRepository.saveAll(this.ctu.getDiscountList());
        final String city = "Unknown City";
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/filter-by-city")
                        .param("city", city)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(0, this.ctu.getDiscountsFromJson(result.getResponse().getContentAsString()).size());
    }

    @Test
    @Order(15)
    void whenFindByCategoryAndDateEmptyResponse() throws Exception {
        // given
        this.discountRepository.saveAll(this.ctu.getDiscountList());
        final String category = "Unknown Category";
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/filter-by-category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("category", category)
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(0, this.ctu.getDiscountsFromJson(result.getResponse().getContentAsString()).size());
    }

    @Test
    @Order(16)
    void whenFindByTypeAndDateEmptyResponse() throws Exception {
        // given
        this.discountRepository.saveAll(this.ctu.getDiscountList());
        String type = "Unknown Type";
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/filter-by-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("type", type)
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(0, this.ctu.getDiscountsFromJson(result.getResponse().getContentAsString()).size());
    }

    @Test
    @Order(17)
    void whenFindBySizeDiscountEmptyResponse() throws Exception {
        // given
        this.discountRepository.saveAll(this.ctu.getDiscountList());
        String discountSize = "Empty Discount Size";
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/filter-by-size")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("size", discountSize)
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(0, this.ctu.getDiscountsFromJson(result.getResponse().getContentAsString()).size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "    "})
    public void whenAddCategoryWrongObligatoryFields(final String description) throws Exception {
        final DiscountEntity discount = this.ctu.getDiscount(this.ctu.getRndEntityPos());
        discount.setDescription(description);
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(discount))
                        .with(csrf()))
                .andReturn();

        // then
        assertEquals(400, result.getResponse().getStatus());
        final String errorResult = Objects.requireNonNull(result.getResolvedException()).getMessage();
        assertTrue(errorResult.contains("must not be blank"));
    }

    @ParameterizedTest
    @ValueSource(strings = {" space at start", "space at end ", " two  spaces  inside", " three   spaces   inside"})
    public void whenAddCategoryTrimFields(final String description) throws Exception {
        final DiscountEntity discount = this.ctu.getDiscount(this.ctu.getRndEntityPos());
        discount.setDescription(description);
        final DiscountEntity postedDiscount;
        String postedDescription;
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(discount))
                        .with(csrf()))
                .andReturn();

        // then
        assertEquals(201, result.getResponse().getStatus());
        postedDiscount = this.ctu.getDiscountFromJson(new JSONObject(result.getResponse().getContentAsString()));
        postedDescription = description.trim();
        while (postedDescription.contains("  "))
            postedDescription = postedDescription.replace("  ", " ");
        assertEquals(postedDescription, postedDiscount.getDescription());
    }
}