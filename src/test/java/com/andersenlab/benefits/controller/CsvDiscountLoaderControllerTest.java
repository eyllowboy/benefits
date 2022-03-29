package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.*;
import com.andersenlab.benefits.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.andersenlab.benefits.controller.ControllerTestUtils.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@WithMockUser
public class CsvDiscountLoaderControllerTest {
    private final DiscountRepository discountRepository;
    private final CompanyRepository companyRepository;
    private final CsvDiscountLoaderRepository csvDiscountLoaderRepository;

    @Autowired
    public CsvDiscountLoaderControllerTest(DiscountRepository discountRepository,
                                           CompanyRepository companyRepository,
                                           CsvDiscountLoaderRepository csvDiscountLoaderRepository) {
        this.discountRepository = discountRepository;
        this.companyRepository = companyRepository;
        this.csvDiscountLoaderRepository = csvDiscountLoaderRepository;
    }

    @Autowired
    MockMvc mockMvc;

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
        clearTables();
    }

    @Test
    public void whenLoadCsvSuccess() throws Exception {
        // given
        final List<DiscountEntity> discounts = getDiscountList();
        final MockMultipartFile csvData = newMockMultipartFile(discounts);
        final List<String> result = new ArrayList<>();
        discounts.forEach(discount -> result.add(discount.getId() + ": OK"));
        final List<DiscountEntity> discountsAfterUpload;
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
            .multipart("/upload-csv-file")
            .file(csvData)
            .with(csrf()))
            .andDo(print())
        // then
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", is(result)));
        discountsAfterUpload = this.discountRepository.findAll();
        assertEquals(discounts.size(), discountsAfterUpload.size());
        for (int i = 0; i < discounts.size(); i++)
            assertTrue(isDiscountsEquals(discounts.get(i), discountsAfterUpload.get(i)));
    }

    @Test
    public void whenLoadCsvDiscountExists() throws Exception {
        // given
        final List<DiscountEntity> discounts = getDiscountList();
        final List<String> result = new ArrayList<>();
        discounts.forEach(discount -> {
            CompanyEntity company = this.csvDiscountLoaderRepository
                    .findCompanyByTitle(discount.getCompany_id().getTitle())
                    .orElse(discount.getCompany_id());
            if (null == company.getId())
                this.companyRepository.save(company);
            discount.setCompany_id(company);
            result.add(discount.getId() + ": SKIP already exists");
        });
        this.discountRepository.saveAll(discounts);
        final MockMultipartFile csvData = newMockMultipartFile(discounts);

        // when
        this.mockMvc.perform(MockMvcRequestBuilders
            .multipart("/upload-csv-file")
            .file(csvData)
            .with(csrf()))
            .andDo(print())
        // then
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", is(result)));

        assertEquals(discounts.size(), this.discountRepository.findAll().size());
    }

    @Test
    public void whenLoadCsvWithDiscountDuplicate() throws Exception {
        // given
        final List<DiscountEntity> discounts = getDiscountList();
        final List<String> result = new ArrayList<>();
        discounts.forEach(discount -> result.add(discount.getId() + ": OK"));
        discounts.add(discounts.get(discounts.size() - 1));
        result.add((discounts.size() - 1) + ": SKIP already exists");
        final MockMultipartFile csvData = newMockMultipartFile(discounts);
        final List<DiscountEntity> discountsAfterUpload;

        // when
        this.mockMvc.perform(MockMvcRequestBuilders
            .multipart("/upload-csv-file")
            .file(csvData)
            .with(csrf()))
            .andDo(print())
        // then
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", is(result)));

        discountsAfterUpload = this.discountRepository.findAll();
        assertEquals(discounts.size() - 1, discountsAfterUpload.size());
        for (int i = 0; i < discounts.size() - 1; i++)
            assertTrue(isDiscountsEquals(discounts.get(i), discountsAfterUpload.get(i)));
    }

    @Test
    public void whenLoadCsvFailLocationNotFound() throws Exception {
        // given
        final List<DiscountEntity> discounts = getDiscountList();
        final Set<LocationEntity> locationToEdit = discounts.get(0).getArea();
        locationToEdit.iterator().next().setCity("Брест");
        final MockMultipartFile csvData = newMockMultipartFile(discounts);

        // when
        this.mockMvc.perform(MockMvcRequestBuilders
            .multipart("/upload-csv-file")
            .file(csvData)
            .with(csrf()))
            .andDo(print())
        // then
            .andExpect(jsonPath("$[0]", is("1: City Брест was not found in database")));
    }

    @Test
    public void whenLoadCsvFailLongField() throws Exception {
        // given
        final List<DiscountEntity> discounts = getDiscountList();
        discounts.get(0).setType("01234567890123456789012345678901234567890123456789012345678901234567890123456789");
        final MockMultipartFile csvData = newMockMultipartFile(discounts);

        // when
        this.mockMvc.perform(MockMvcRequestBuilders
            .multipart("/upload-csv-file")
            .file(csvData)
            .with(csrf()))
            .andDo(print())
        // then
            .andExpect(jsonPath("$[0]", is("1: org.postgresql.util.PSQLException: ERROR: value too long for type character varying(50)")));
    }

    @Test
    public void whenLoadCsvFailIncorrectNumberOfDelimitedFields() throws Exception {
        // given
        final List<DiscountEntity> discounts = getDiscountList();
        discounts.get(0).setType("0123456789;0123456789");
        final MockMultipartFile csvData = newMockMultipartFile(discounts);

        // when
        this.mockMvc.perform(MockMvcRequestBuilders
            .multipart("/upload-csv-file")
            .file(csvData)
            .with(csrf()))
            .andDo(print())
        // then
            .andExpect(jsonPath("$[0]", is("1: Number of delimited fields does not match header")));
    }
}
