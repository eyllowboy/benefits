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

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.random;
import static java.sql.Timestamp.valueOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@WithMockUser
public class CsvDiscountLoaderControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    DiscountRepository discountRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    CsvDiscountLoaderRepository csvDiscountLoaderRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    UserRepository userRepository;

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

    private Set<CategoryEntity> getCategoryList() {
        Set<CategoryEntity> result = new LinkedHashSet<>();
        final int size = (int) (random() * 10 + 1);
        for (long i = 1; i <= size; i++) {
            CategoryEntity category = this.categoryRepository.findByTitle("Category" + i)
                    .orElse(new CategoryEntity("Category" + i));
            if (null == category.getId())
                categoryRepository.save(category);
            result.add(category);
        }
        return result;
    }

    private Set<LocationEntity> getLocationList() {
        Set<LocationEntity> result = new LinkedHashSet<>();
        final int size = (int) (random() * 10 + 1);
        for (long i = 1; i <= size; i++) {
            LocationEntity location = this.csvDiscountLoaderRepository.findLocationByCity("City" + i)
                    .orElse(new LocationEntity("SomeCountry", "City" + i));
            if (null == location.getId())
                locationRepository.save(location);
            result.add(location);
        }
        return result;
    }

    private CompanyEntity getCompany() {
        final long num = (long) (random() * 10 + 1);
        return (new CompanyEntity(
                        "Company" + num,
                        "Description" + num,
                        "Address" + num,
                        "Phone" + num,
                        "Link" + num
                    ));
    }

    private List<DiscountEntity> getDiscountList(final int discountsCount) {
        List<DiscountEntity> result = new ArrayList<>();
        for (long i = 1; i <= discountsCount; i++) {
            result.add(new DiscountEntity(
                            i,
                            "Type" + i,
                            "Description" + i,
                            "Condition" + i,
                            "Size" + i,
                            DiscountType.DISCOUNT,
                            valueOf("2022-01-01 00:00:00"),
                            valueOf("2022-12-31 00:00:00"),
                            "Image" + i,
                            getLocationList(),
                            getCategoryList(),
                            getCompany()));
        }
        return result;
    }

    private String discountToString(final DiscountEntity discount) {
        return (
                discount.getId() + ";" +
                discount.getCompany_id().getTitle() + ";" +
                discount.getType() + ";" +
                discount.getCategories().stream().map(CategoryEntity::getTitle).collect(Collectors.joining("|")) + ";" +
                discount.getImageDiscount() + ";" +
                discount.getCompany_id().getDescription() + ";" +
                discount.getCompany_id().getAddress() + ";" +
                discount.getCompany_id().getPhone() + ";" +
                discount.getCompany_id().getLink() + ";" +
                discount.getSizeDiscount() + ";" +
                discount.getDiscount_type() + ";" +
                discount.getDescription() + ";" +
                discount.getDiscount_condition() + ";" +
                discount.getDateBegin() + ";" +
                discount.getDateFinish() + ";" +
                discount.getArea().stream().map(LocationEntity::getCity).collect(Collectors.joining("|"))
                );
    }

    private MockMultipartFile newMockMultipartFile(final List<DiscountEntity> discounts) {
        StringBuilder contents = new StringBuilder("number;company_title;type;category;image;company_description;company_address;company_phone;links;size;discount_type;discount_description;discount_condition;start_date;end_date;location");
        discounts.forEach(discount -> contents.append("\n").append(discountToString(discount)));
        return (new MockMultipartFile(
                "file",
                "discounts.csv",
                "multipart/form-data",
                contents.toString().getBytes(StandardCharsets.UTF_8)));
    }

    private boolean isCompaniesEquals(final CompanyEntity company1, final CompanyEntity company2) {
        return (
                company1.getTitle().equals(company2.getTitle()) &&
                company1.getAddress().equals(company2.getAddress()) &&
                company1.getDescription().equals(company2.getDescription()) &&
                company1.getPhone().equals(company2.getPhone()) &&
                company1.getLink().equals(company2.getLink())
                );
    }

    private boolean isDiscountsEquals(final DiscountEntity discount1, final DiscountEntity discount2) {
        if (discount1 == discount2) return true;
        if (null == discount1 || discount1.getClass() != discount2.getClass()) return false;
        return (
                discount1.getType().equals(discount2.getType()) &&
                discount1.getDescription().equals(discount2.getDescription()) &&
                discount1.getDiscount_condition().equals(discount2.getDiscount_condition()) &&
                discount1.getSizeDiscount().equals(discount2.getSizeDiscount()) &&
                discount1.getImageDiscount().equals(discount2.getImageDiscount()) &&
                isCompaniesEquals(discount1.getCompany_id(), discount2.getCompany_id())
        );
    }

    @BeforeEach
    private void clearData() {
        this.discountRepository.deleteAll();
        this.categoryRepository.deleteAll();
        this.companyRepository.deleteAll();
        this.userRepository.deleteAll();
        this.locationRepository.deleteAll();
    }

    @Test
    public void whenLoadCsvSuccess() throws Exception {
        // given
        final int discountsCount = 10;
        final List<DiscountEntity> discounts = getDiscountList(discountsCount);
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
        assertEquals(discountsCount, discountsAfterUpload.size());
        for (int i = 0; i < discounts.size(); i++)
            assertTrue(isDiscountsEquals(discounts.get(i), discountsAfterUpload.get(i)));
    }

    @Test
    public void whenLoadCsvDiscountExists() throws Exception {
        // given
        final int discountsCount = 20;
        final List<DiscountEntity> discounts = getDiscountList(discountsCount);
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

        assertEquals(discountsCount, this.discountRepository.findAll().size());
    }

    @Test
    public void whenLoadCsvWithDiscountDuplicate() throws Exception {
        // given
        int discountsCount = 3;
        final List<DiscountEntity> discounts = getDiscountList(discountsCount);
        final List<String> result = new ArrayList<>();
        discounts.forEach(discount -> result.add(discount.getId() + ": OK"));
        discounts.add(discounts.get(discountsCount - 1));
        result.add(discountsCount + ": SKIP already exists");
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
        assertEquals(discountsCount, discountsAfterUpload.size());
        for (int i = 0; i < discountsCount; i++)
            assertTrue(isDiscountsEquals(discounts.get(i), discountsAfterUpload.get(i)));
    }

    @Test
    public void whenLoadCsvFailLocationNotFound() throws Exception {
        // given
        int discountsCount = 1;
        final List<DiscountEntity> discounts = getDiscountList(discountsCount);
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
        int discountsCount = 10;
        final List<DiscountEntity> discounts = getDiscountList(discountsCount);
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
        int discountsCount = 10;
        final List<DiscountEntity> discounts = getDiscountList(discountsCount);
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
