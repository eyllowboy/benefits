package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.CompanyEntity;
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

import static com.andersenlab.benefits.service.impl.ValidateUtils.*;
import static java.lang.Math.random;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@WithMockUser
class CompanyControllerTest {

    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    private final DiscountRepository discountRepository;
    private final CompanyRepository companyRepository;
    private final ControllerTestUtils ctu;

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres")
                    .withDatabaseName("benefits")
                    .withUsername("benefits")
                    .withPassword("ben0147");

    @Autowired
    CompanyControllerTest(final ObjectMapper objectMapper,
                          final MockMvc mockMvc,
                          final DiscountRepository discountRepository,
                          final CompanyRepository companyRepository,
                          final ControllerTestUtils ctu) {
        this.objectMapper = objectMapper;
        this.mockMvc = mockMvc;
        this.discountRepository = discountRepository;
        this.companyRepository = companyRepository;
        this.ctu = ctu;
    }

    @DynamicPropertySource
    public static void postgreSQLProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    }

    @BeforeEach
    public void clearData() {
        this.ctu.clearTables();
    }

    @Test
    void whenAddCompanySuccess() throws Exception {
        // given
        final CompanyEntity company = new CompanyEntity("title6", "description6", "address6", "phone6", "link6");
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                .post("/companies")
                .content(this.objectMapper.writeValueAsString(company))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(201, result.getResponse().getStatus());
        assertEquals(1, this.companyRepository.findAll().size());
        assertTrue(this.ctu.isCompaniesEquals(
                company,
                this.ctu.getCompanyFromJson(new JSONObject(result.getResponse().getContentAsString()))));
    }

    @Test
    void whenGetCompanyByIdSuccess() throws Exception {
        // given
        final CompanyEntity company = this.ctu.getCompanyList().get(this.ctu.getRndEntityPos() - 1);
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                .get("/companies/{id}", company.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        assertTrue(this.ctu.isCompaniesEquals(
                company,
                this.ctu.getCompanyFromJson(new JSONObject(result.getResponse().getContentAsString()))));
    }

    @Test
    void whenGetSomeSizeCompanyIsOk() throws Exception {
        // given
        this.ctu.getCompanyList();
        final int rndSize = (int) (random() * (5 - 1) + 1);
        final Page<CompanyEntity> foundCompany = this.companyRepository.findAll(PageRequest.of(0, rndSize));
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                .get("/companies?page=0&size=" + rndSize)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andDo(print())
                .andReturn();

        // then
        final RestResponsePage<CompanyEntity> pageResult = this.objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {});
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(foundCompany, pageResult);
    }

    @Test
    void whenGetCompanyWithIncorrectId() {
        //given
        final Long id = 0L;

        //when
        final NestedServletException NestedServletException = assertThrows(NestedServletException.class,
                () -> this.mockMvc.perform(get("/companies/{id}", id).with(csrf())));
        //then
        assertEquals(IllegalStateException.class,
                NestedServletException.getCause().getClass());
        assertEquals(errIdNotFoundMessage("Company", id),
                NestedServletException.getCause().getMessage());
    }

    @Test
    void whenUpdateCompanySuccess() throws Exception {
        //given
        final CompanyEntity company = this.ctu.getCompanyList().get(this.ctu.getRndEntityPos() - 1);
        company.setTitle("New Title");
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                .patch("/companies/{id}", company.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(company))
                .with(csrf()))
                .andDo(print())
                .andReturn();

        //then
        assertEquals(200, result.getResponse().getStatus());
        assertTrue(this.ctu.isCompaniesEquals(
                company,
                this.companyRepository.findById(company.getId()).orElseThrow()));
    }

    @Test
    void whenUpdateCompanyTitleExists() {
        // when
        this.ctu.getCompany(1);
        final CompanyEntity company = this.ctu.getCompany(2);
        company.setTitle("Company1");

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> this.mockMvc.perform(MockMvcRequestBuilders
                    .patch("/companies/{id}", company.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(company))
                    .with(csrf())));

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals(errAlreadyExistMessage("Company", "title", company.getTitle()),
                nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenDeleteCompanySuccess() throws Exception {
        // given
        final CompanyEntity company = this.ctu.getCompanyList().get(this.ctu.getRndEntityPos() - 1);
        final int sizeBeforeDelete = this.companyRepository.findAll().size();
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                    .delete("/companies/{id}", company.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()))
                    .andDo(print())
                    .andReturn();

        // then
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(sizeBeforeDelete - 1, this.companyRepository.findAll().size());
        assertEquals(Optional.empty(), this.companyRepository.findById(company.getId()));
    }

    @Test
    public void whenDeleteCompanyIdNotExists() {
        // given
        final Long id = Long.MAX_VALUE;

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/companies/{id}", id)
                        .with(csrf())));

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals(errIdNotFoundMessage("Company", id),
                nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenDeleteCompanyFailHasActiveDiscounts() {
        // given
        final DiscountEntity discount = this.ctu.getDiscount(this.ctu.getRndEntityPos());
        final CompanyEntity company = this.companyRepository.save(discount.getCompany());
        this.discountRepository.save(discount);

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                    .delete("/companies/{id}", company.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf())));

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals(errAssociatedEntity("discounts", "Company"),
                nestedServletException.getCause().getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "    "})
    public void whenAddCompanyWrongObligatoryFields(final String title) {
        // given
        final CompanyEntity company = new CompanyEntity(title, "description6", "address6", "phone6", "link6");
        company.setTitle(title);

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/companies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(company))
                    .with(csrf()))
                    .andReturn());

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertTrue(nestedServletException.getCause().getMessage().contains("has no data"));
    }

    @ParameterizedTest
    @ValueSource(strings = {" space at start", "space at end ", " two  spaces  inside", " three   spaces   inside"})
    public void whenAddCompanyTrimFields(final String title) throws Exception {
        // given
        final CompanyEntity company = new CompanyEntity(title, "description6", "address6", "phone6", "link6");
        final CompanyEntity postedCompany;
        String postedTitle;
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/companies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(company))
                    .with(csrf()))
                    .andReturn();

        // then
        assertEquals(201, result.getResponse().getStatus());
        postedCompany = this.ctu.getCompanyFromJson(new JSONObject(result.getResponse().getContentAsString()));
        postedTitle = title.trim();
        while (postedTitle.contains("  "))
            postedTitle = postedTitle.replace("  ", " ");
        assertEquals(postedTitle, postedCompany.getTitle());
    }

    @ParameterizedTest
    @ValueSource(strings = {"101"})
    public void whenAddCompanyWrongFieldSize(final Integer stringSize) {
        // given
        final String fieldValue = "a".repeat(stringSize);
        final CompanyEntity company = this.ctu.getCompany(this.ctu.getRndEntityPos());
        company.setTitle(fieldValue);

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/companies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(company))
                    .with(csrf()))
                    .andReturn());

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertTrue(nestedServletException.getCause().getMessage().contains("must be between"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"51", "101"})
    public void whenUpdateCompanyWrongFieldSize(final Integer stringSize) {
        // given
        final CompanyEntity company = this.ctu.getCompany(this.ctu.getRndEntityPos());
        final String fieldValue = "a".repeat(stringSize);
        company.setTitle(fieldValue);

        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/companies/{id}", company.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(company))
                        .with(csrf())));

        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertTrue(nestedServletException.getCause().getMessage().contains("must be between"));
    }
}
