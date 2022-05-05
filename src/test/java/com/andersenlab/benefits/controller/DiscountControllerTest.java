package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.repository.DiscountRepository;
import com.andersenlab.benefits.support.RestResponsePage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

import java.util.List;
import java.util.Optional;

import static com.andersenlab.benefits.service.impl.ValidateUtils.errIdNotFoundMessage;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    public static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres")
                    .withDatabaseName("benefits")
                    .withUsername("benefits")
                    .withPassword("ben0147");

    @DynamicPropertySource
    public static void postgreSQLProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    }

    @BeforeEach
    public void clearData() {
        this.ctu.clearTables();
    }

    @Test
    public void whenGetSomeSizeDiscountsSuccess() throws Exception {
        // given
        this.discountRepository.saveAll(this.ctu.getDiscountList());
        final int rndSize = this.ctu.getRndEntityPos();
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts?page=0&size=" + rndSize)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        // then

        final RestResponsePage<DiscountEntity> pageResult = this.objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(rndSize, pageResult.getContent().size());
    }

    @Test
    public void whenGetDiscountByIdSuccess() throws Exception {
        // given
        final DiscountEntity discount = this.discountRepository.saveAll(
                this.ctu.getDiscountList()).get(this.ctu.getRndEntityPos() - 1);
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/{id}", discount.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();
        // then
        assertEquals(200, result.getResponse().getStatus());
        assertTrue(this.ctu.isDiscountsEquals(
                discount, this.ctu.getDiscountFromJson(new JSONObject(result.getResponse().getContentAsString()))));
    }

    @Test
    public void whenGetDiscountByIdFailNotExists() {
        // given
        final long id = this.ctu.getRndEntityPos();

        // when
        final NestedServletException NestedServletException = assertThrows(NestedServletException.class,
                () -> this.mockMvc.perform(get("/discounts/{id}", id).with(csrf())));

        // then
        assertEquals(IllegalStateException.class, NestedServletException.getCause().getClass());
        assertEquals(errIdNotFoundMessage("Discount", id), NestedServletException.getCause().getMessage());
        assertEquals(Optional.empty(), this.discountRepository.findById(id));
    }

    @Test
    public void whenAddDiscountSuccess() throws Exception {
        // given
        final DiscountEntity discount = this.ctu.getDiscount(this.ctu.getRndEntityPos());
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/discounts")
                        .with(csrf())
                        .content(this.objectMapper.writeValueAsString(discount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(201, result.getResponse().getStatus());
        assertEquals(1, this.discountRepository.findAll().size());
        assertTrue(this.ctu.isDiscountsEquals(
                discount, this.ctu.getDiscountFromJson(new JSONObject(result.getResponse().getContentAsString()))));
    }

    @Test
    public void whenUpdateDiscountSuccess() throws Exception {
        // given
        final DiscountEntity discount = this.discountRepository.saveAll(
                this.ctu.getDiscountList()).get(this.ctu.getRndEntityPos() - 1);
        discount.setDescription("New Discount Description");
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/discounts/{id}", discount.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(discount)))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        assertTrue(this.ctu.isDiscountsEquals(
                discount, this.discountRepository.findById(discount.getId()).orElseThrow()));
    }

    @Test
    public void whenUpdateDiscountFailIdNotExists() {
        // given
        final DiscountEntity discount = this.discountRepository.saveAll(
                this.ctu.getDiscountList()).get(this.ctu.getRndEntityPos() - 1);
        discount.setDescription("New Discount Description");
        discount.setId(Long.MAX_VALUE);

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/discounts/{id}", discount.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(discount))));

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals(errIdNotFoundMessage("Discount", discount.getId()),
                nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenDeleteDiscountSuccess() throws Exception {
        // given
        final DiscountEntity discount = this.discountRepository.saveAll(
                this.ctu.getDiscountList()).get(this.ctu.getRndEntityPos() - 1);
        final int sizeBeforeDelete = this.discountRepository.findAll().size();
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/discounts/{id}", discount.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(sizeBeforeDelete - 1, this.discountRepository.findAll().size());
        assertEquals(Optional.empty(), this.discountRepository.findById(discount.getId()));
    }


    @Test
    public void whenDeleteDiscountFailIdNotExists() {
        // given
        final Long id = 0L;

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/discounts/{id}", id)
                        .with(csrf())));

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals(errIdNotFoundMessage("Discount", id), nestedServletException.getCause().getMessage());
    }

    @Test
    void whenFindByCityAndDateSuccess() throws Exception {
        // given
        final DiscountEntity discount = this.discountRepository.saveAll(
                this.ctu.getDiscountList()).get(this.ctu.getRndEntityPos() - 1);
        final String city = (discount.getArea().stream().findFirst().orElseThrow()).getCity();
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/find-by-city")
                        .param("city", city)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        final RestResponsePage<DiscountEntity> pageResult = this.objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        pageResult.getContent().forEach(item -> assertTrue(item.getArea().stream()
                .anyMatch(areaCity -> areaCity.getCity().equals(city))));
    }

    @Test
    void whenFindByCategoryAndDateSuccess() throws Exception {
        // given
        final DiscountEntity discount = this.discountRepository.saveAll(
                this.ctu.getDiscountList()).get(this.ctu.getRndEntityPos() - 1);
        final String category = (discount.getCategories().stream().findFirst().orElseThrow()).getTitle();
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/find-by-category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("category", category)
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        final RestResponsePage<DiscountEntity> pageResult = this.objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        pageResult.getContent().forEach(item -> assertTrue(item.getCategories().stream()
                .anyMatch(areaCategory -> areaCategory.getTitle().equals(category))));
    }

    @Test
    void whenFindByTypeAndDateSuccess() throws Exception {
        // given
        final DiscountEntity discount = this.discountRepository.saveAll(
                this.ctu.getDiscountList()).get(this.ctu.getRndEntityPos() - 1);
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/find-by-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("type", discount.getType())
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        final RestResponsePage<DiscountEntity> pageResult = this.objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        pageResult.getContent().forEach(item -> assertEquals(item.getType(), discount.getType()));
    }

    @Test
    void whenFindBySizeDiscountSuccess() throws Exception {
        // given
        final DiscountEntity discount = this.discountRepository.saveAll(
                this.ctu.getDiscountList()).get(this.ctu.getRndEntityPos() - 1);
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/find-by-size")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("sizeDiscount", discount.getSizeDiscount())
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        final RestResponsePage<DiscountEntity> pageResult = this.objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        pageResult.getContent().forEach(item -> assertEquals(item.getSizeDiscount(), discount.getSizeDiscount()));
    }

    @Test
    void whenFindByCityAndDateEmptyResponse() throws Exception {
        // given
        this.discountRepository.saveAll(this.ctu.getDiscountList());
        final String city = "Unknown City";
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/find-by-city")
                        .param("city", city)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        final RestResponsePage<DiscountEntity> pageResult = this.objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(0, pageResult.getContent().size());
    }

    @Test
    void whenFindByCategoryAndDateEmptyResponse() throws Exception {
        // given
        this.discountRepository.saveAll(this.ctu.getDiscountList());
        final String category = "Unknown Category";
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/find-by-category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("category", category)
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        final RestResponsePage<DiscountEntity> pageResult = this.objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(0, pageResult.getContent().size());
    }

    @Test
    void whenFindByTypeAndDateEmptyResponse() throws Exception {
        // given
        this.discountRepository.saveAll(this.ctu.getDiscountList());
        final String type = "Unknown Type";
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/find-by-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("type", type)
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        final RestResponsePage<DiscountEntity> pageResult = this.objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(0, pageResult.getContent().size());
    }

    @Test
    void whenFindBySizeDiscountEmptyResponse() throws Exception {
        // given
        this.discountRepository.saveAll(this.ctu.getDiscountList());
        final String discountSize = "Empty Discount Size";
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/find-by-size")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("sizeDiscount", discountSize)
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        final RestResponsePage<DiscountEntity> pageResult = this.objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(0, pageResult.getContent().size());
    }

    @ParameterizedTest
    @CsvSource({
            "Category1, Size1",
            "Category2, Size2",
            "Category3, Size3",
            "Category4, Size4"
    })
    void whenFindSimilar(final String title, final String size) throws Exception {
        // given
        this.discountRepository.saveAll(this.ctu.getDiscountList());
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/discounts/find-similar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("category", title)
                        .param("sizeDiscount", size)
                        .param("limit", "3")
                        .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        final List<DiscountEntity> foundDiscounts = this.ctu.getDiscountsFromJson(result.getResponse().getContentAsString());
        foundDiscounts.forEach(discount ->
                assertTrue(discount.getCategories().stream().anyMatch(category ->
                        category.getTitle().contains(title))
                        && (discount.getSizeDiscount().contains(size)
                        || size.contains(discount.getSizeDiscount()))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "    "})
    public void whenAddDiscountWrongObligatoryFields(final String description) {
        // given
        final DiscountEntity discount = this.ctu.getDiscount(this.ctu.getRndEntityPos());
        discount.setDescription(description);

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(discount))
                        .with(csrf())));

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertTrue(nestedServletException.getCause().getMessage().contains("has no data"));
    }

    @ParameterizedTest
    @ValueSource(strings = {" space at start", "space at end ", " two  spaces  inside", " three   spaces   inside"})
    public void whenAddDiscountTrimFields(final String description) throws Exception {
        // given
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

    @ParameterizedTest
    @ValueSource(strings = {"150"})
    public void whenAddDiscountWrongFieldSize(final Integer stringSize) {
        // given
        final String fieldValue = "a".repeat(stringSize);
        final DiscountEntity discount = this.ctu.getDiscount(this.ctu.getRndEntityPos());
        discount.setSizeDiscount(fieldValue);

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(discount))
                        .with(csrf())));

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertTrue(nestedServletException.getCause().getMessage().contains("must be between"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"150"})
    public void whenUpdateDiscountWrongFieldSize(final Integer stringSize) {
        final DiscountEntity discount = this.discountRepository.saveAll(
                this.ctu.getDiscountList()).get(this.ctu.getRndEntityPos() - 1);
        final String fieldValue = "a".repeat(stringSize);
        discount.setSizeDiscount(fieldValue);

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/discounts/{id}", discount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(discount))
                        .with(csrf())));

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertTrue(nestedServletException.getCause().getMessage().contains("must be between"));
    }
}
