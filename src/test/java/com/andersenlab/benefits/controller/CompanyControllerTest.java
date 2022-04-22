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
import static java.lang.Math.random;
import java.util.Objects;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private void deleteAndSaveCompanyInContainer() {
        this.ctu.clearTables();
        createAndSaveCompanyInContainer();
    }

    private void createAndSaveCompanyInContainer() {
        final int size = 5;
        for (long i = 1; i <= size; i++) {
            CompanyEntity company = new CompanyEntity("title" + i, "description" + i, "address" + i, "phone" + i, "title" + i);
            this.companyRepository.save(company);
        }
    }

    @Test
    void whenAddCompanyWithPositiveScenario() throws Exception {
        // given
        final CompanyEntity company = new CompanyEntity(6L, "company6", "description6", "address6", "8900-00-00", "link06");
        // when
        this.mockMvc.perform(
                        post("/companies")
                                .content(this.objectMapper.writeValueAsString(company))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andDo(print())
                // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", isA(Number.class)))
                .andExpect(jsonPath("$.title", is("company6")));
    }

    @Test
    void whenGetCompanyByIdIsOk() throws Exception {
        // given
        final String companyTitle = "title6";
        final CompanyEntity company = new CompanyEntity(companyTitle, "description6", "address6", "phone6", "title6");
        final CompanyEntity saveEntity = this.companyRepository.save(company);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/companies/{id}", saveEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(saveEntity.getId().intValue())))
                .andExpect(jsonPath("$.title", is(companyTitle)));
    }

    @Test
    void whenGetSomeSizeCompanyIsOk() throws Exception {
        // given
        final int rndSize = (int) (random() * (5 - 1) + 1);
        final Page<CompanyEntity> foundCompany = companyRepository.findAll(PageRequest.of(0, rndSize));
        final MvcResult result;
        //when
        result =this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/companies?page=0&size="+rndSize)
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
        final CompanyEntity company = new CompanyEntity("title6", "description6", "address6", "phone6", "title6");
        final CompanyEntity saveEntity = this.companyRepository.save(company);
        final long notExistId = saveEntity.getId() + 1;
        //when
        NestedServletException NestedServletException = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/companies/{id}", notExistId).with(csrf())));
        //then
        assertEquals(IllegalStateException.class,
                NestedServletException.getCause().getClass());
        assertEquals("Company with this id was not found in the database.",
                NestedServletException.getCause().getMessage());
    }

    @Test
    void whenUpdatedCompanyPositiveScenario() throws Exception {
        //given
        final CompanyEntity company = new CompanyEntity("title6", "description6", "address6", "phone6", "title6");
        final CompanyEntity saveEntity = this.companyRepository.save(company);
        final CompanyEntity updateCompany = new CompanyEntity(saveEntity.getId(), "company22", "description22", "address22", "8911-11-11", "link22");
        final String companyEntity = this.objectMapper.writeValueAsString(updateCompany);
        //when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/companies/{id}", saveEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(companyEntity)
                        .with(csrf()))
                .andDo(print())
                //then
                .andExpect(status().isOk());
    }

    @Test
    void whenUpdatedCompanyNegativeScenario() throws Exception {
        // when
        final CompanyEntity company = new CompanyEntity("title6", "description6", "address6", "phone6", "title6");
        final CompanyEntity saveEntity = this.companyRepository.save(company);
        final long notExistId = saveEntity.getId() + 1;
        final CompanyEntity updatedCompany = new CompanyEntity(notExistId, "company9", "description9", "address9", "8911-11-11", "link9");
        final String companyEntity = this.objectMapper.writeValueAsString(updatedCompany);
        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/companies/{id}", notExistId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(companyEntity)
                        .with(csrf())));
        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("The company with id: " + notExistId + " was not found in the database.", nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenDeletePositiveScenarioWithoutDiscount() throws Exception {
        // given
        final CompanyEntity company = new CompanyEntity("title6", "description6", "address6", "phone6", "title6");
        final CompanyEntity saveEntity = this.companyRepository.save(company);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/companies/{id}", saveEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                // then
                .andExpect(status().isOk());
    }

    @Test
    public void whenDeleteNegativeScenario() {
        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/companies/{id}", Long.MAX_VALUE)
                        .with(csrf())));
        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("The company with id: " + Long.MAX_VALUE + " was not found in the database.",
                nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenDeleteCompanyFailHasActiveDiscounts() {
        // given
        final CompanyEntity companyWithActiveDiscount = this.companyRepository.save(this.ctu.getCompany(6));
        final DiscountEntity discount = this.ctu.getDiscount(this.ctu.getRndEntityPos());
        discount.setCompany(companyWithActiveDiscount);
        this.discountRepository.save(discount);
        final Long id = companyWithActiveDiscount.getId();
        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())));
        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("There is active discounts in this Category in database",
                nestedServletException.getCause().getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "    "})
    public void whenAddCompanyWrongObligatoryFields(final String title) throws Exception {
        // given
        final CompanyEntity company = new CompanyEntity(title, "description6", "address6", "phone6", "link6");
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/companies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(company))
                    .with(csrf()))
                .andReturn();

        // then*
        assertEquals(400, result.getResponse().getStatus());
        final String errorResult = Objects.requireNonNull(result.getResolvedException()).getMessage();
        assertTrue(errorResult.contains("must not be blank"));
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
    @ValueSource(strings = {"0", "101"})
    public void whenAddCompanyWrongFieldSize(final Integer stringSize) throws Exception {
        // given
        final String fieldValue = "a".repeat(stringSize);
        final CompanyEntity company = this.ctu.getCompany(this.ctu.getRndEntityPos());
        company.setTitle(fieldValue);
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(company))
                        .with(csrf()))
                .andReturn();

        // then
        assertEquals(400, result.getResponse().getStatus());
        final String errorResult = Objects.requireNonNull(result.getResolvedException()).getMessage();
        assertTrue(errorResult.contains("must be between"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"51", "101"})
    public void whenUpdateCompanyWrongFieldSize(final Integer stringSize) throws Exception {
        // given
        final CompanyEntity company = this.companyRepository.save(this.ctu.getCompany(this.ctu.getRndEntityPos()));
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
