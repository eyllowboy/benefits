package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.CompanyEntity;
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
import static org.hamcrest.Matchers.*;
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

    private final LocationRepository locationRepository;

    private  final RoleRepository roleRepository;

    private final UserRepository userRepository;

    private  final DiscountRepository discountRepository;

    private final CompanyRepository companyRepository;

    private  final CategoryRepository categoryRepository;

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres")
                    .withDatabaseName("benefits")
                    .withUsername("benefits")
                    .withPassword("ben0147");

    @Autowired
    CompanyControllerTest(ObjectMapper objectMapper, MockMvc mockMvc, LocationRepository locationRepository, RoleRepository roleRepository, UserRepository userRepository, DiscountRepository discountRepository, CompanyRepository companyRepository, CategoryRepository categoryRepository) {
        this.objectMapper = objectMapper;
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
    private void deleteAndSaveCompanyInContainer() {
        this.discountRepository.deleteAll();
        this.categoryRepository.deleteAll();
        this.companyRepository.deleteAll();
        this.userRepository.deleteAll();
        this.locationRepository.deleteAll();
        this.roleRepository.deleteAll();
        createAndSaveCompanyInContainer();
    }

    private void createAndSaveCompanyInContainer() {
        final int size = 5;
        for (long i = 1; i <= size; i++) {
            CompanyEntity company = new CompanyEntity("title" + i, "description" + i, "address" + i, "phone" + i, "title" + i);
            companyRepository.save(company);
        }
    }

    @Test
    void whenAddCompanyWithPositiveScenario() throws Exception {
        // given
        final CompanyEntity company = new CompanyEntity(6L, "company6", "description6", "address6", "8900-00-00", "link06");
        // when
        this.mockMvc.perform(
                        post("/companies")
                                .content(objectMapper.writeValueAsString(company))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andDo(print())
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", isA(Number.class)))
                .andExpect(jsonPath("$.title", is("company6")));
    }

    @Test
    void whenGetCompanyByIdIsOk() throws Exception {
        // given
        final String companyTitle = "title6";
        final CompanyEntity company = new CompanyEntity(companyTitle, "description6", "address6", "phone6", "title6");
        final CompanyEntity saveEntity = companyRepository.save(company);
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
    void whenGetAllCompanyIsOk() throws Exception {
        //when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void whenGetCompanyWithIncorrectId() {
        //given
        final CompanyEntity company = new CompanyEntity("title6", "description6", "address6", "phone6", "title6");
        final CompanyEntity saveEntity = companyRepository.save(company);
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
        final CompanyEntity saveEntity = companyRepository.save(company);
        final CompanyEntity updateCompany = new CompanyEntity(saveEntity.getId(), "company22", "description22", "address22", "8911-11-11", "link22");
        final String companyEntity = new ObjectMapper().writeValueAsString(updateCompany);
        //when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .put("/companies/{id}", saveEntity.getId())
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
        final CompanyEntity saveEntity = companyRepository.save(company);
        final long notExistId = saveEntity.getId() + 1;
        final CompanyEntity updatedCompany = new CompanyEntity(notExistId, "company9", "description9", "address9", "8911-11-11", "link9");
        final String companyEntity = new ObjectMapper().writeValueAsString(updatedCompany);
        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(MockMvcRequestBuilders
                        .put("/companies/{id}", notExistId)
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
        final CompanyEntity saveEntity = companyRepository.save(company);
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
        final CompanyEntity companyWithActiveDiscount = new CompanyEntity("title6", "description6", "address6", "phone6", "title6");
        final DiscountEntity discount = new DiscountEntity();
        discount.setType("type");
        discount.setDescription("description");
        discount.setDiscount_condition("discount_condition");
        discount.setSizeDiscount("10");
        discount.setDiscount_type(DiscountType.DISCOUNT);
        discount.setDateBegin(new Date());
        discount.setDateFinish(new Date());
        discount.setImageDiscount("imageDiscount");
        var savedCompany = companyRepository.save(companyWithActiveDiscount);
        discount.setCompany(savedCompany);
        discountRepository.save(discount);
        final Long id = savedCompany.getId();
        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                mockMvc.perform(MockMvcRequestBuilders
                        .delete("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())));
        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("There is active discounts in this Category in database",
                nestedServletException.getCause().getMessage());
    }
}