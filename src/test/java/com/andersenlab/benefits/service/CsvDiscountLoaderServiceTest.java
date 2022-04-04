package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.*;
import com.andersenlab.benefits.repository.*;
import com.andersenlab.benefits.service.impl.CsvDiscountLoaderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static com.andersenlab.benefits.service.ServiceTestUtils.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "spring.main.lazy-initialization=true",
        classes = {CsvDiscountLoaderService.class, CsvDiscountLoaderServiceImpl.class})
public class CsvDiscountLoaderServiceTest {

    private final List<CompanyEntity> companies = new ArrayList<>();
    private final List<LocationEntity> locations = new ArrayList<>();
    private final List<CategoryEntity> categories = new ArrayList<>();
    private final List<DiscountEntity> discounts = new ArrayList<>();
    private final CsvDiscountLoaderServiceImpl csvDiscountLoaderService;

    @MockBean
    private final DiscountRepository discountRepository;
    @MockBean
    private final CompanyRepository companyRepository;
    @MockBean
    private final CategoryRepository categoryRepository;
    @MockBean
    private final LocationRepository locationRepository;

    @Autowired
    public CsvDiscountLoaderServiceTest(final CsvDiscountLoaderServiceImpl csvDiscountLoaderService,
                                        final DiscountRepository discountRepository,
                                        final CompanyRepository companyRepository,
                                        final CategoryRepository categoryRepository,
                                        final LocationRepository locationRepository) {
        this.csvDiscountLoaderService = csvDiscountLoaderService;
        this.discountRepository = discountRepository;
        this.companyRepository = companyRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
    }

    @BeforeEach
    public void ResetData() {
        this.companies.clear();
        this.locations.clear();
        this.categories.clear();
        this.discounts.clear();

        when(this.companyRepository.save(any())).thenAnswer(invocation ->
                    saveItem(this.companies, invocation.getArgument(0), ServiceTestUtils::isCompaniesEquals));

        when(this.locationRepository.save(any())).thenAnswer(invocation ->
                saveItem(this.locations, invocation.getArgument(0), Objects::equals));
        when(this.locationRepository.findAll()).thenReturn(this.locations);

        when(this.categoryRepository.save(any())).thenAnswer(invocation ->
                saveItem(this.categories, invocation.getArgument(0), Objects::equals));
        when(this.categoryRepository.findAll()).thenReturn(this.categories);
        when(this.categoryRepository.findByTitle(any())).thenAnswer(invocation -> {
            CategoryEntity result = this.categories.stream().filter(category ->
                    Objects.equals(category.getTitle(), invocation.getArgument(0))).findFirst().orElse(null);
            return result != null ? Optional.of(result) : Optional.empty();
        });

        when(this.discountRepository.save(any())).thenAnswer(invocation -> {
            DiscountEntity discount = invocation.getArgument(0);
            discount.setCompany_id(saveItem(this.companies, discount.getCompany_id(), ServiceTestUtils::isCompaniesEquals));
            return saveItem(this.discounts, discount, ServiceTestUtils::isDiscountsEquals);
        });
        when(this.discountRepository.saveAll(any())).thenAnswer(invocation -> {
            List<DiscountEntity> items = invocation.getArgument(0);
            List<DiscountEntity> result = new ArrayList<>();
            items.forEach(item -> {
                item.setCompany_id(saveItem(this.companies, item.getCompany_id(), ServiceTestUtils::isCompaniesEquals));
                result.add(saveItem(this.discounts, item, ServiceTestUtils::isDiscountsEquals));
            });
            saveDiscountsParameters(this.discounts);
            return result;
        });
        when(this.discountRepository.findAll()).thenReturn(this.discounts);
    }

    private void saveDiscountsParameters(final Collection<DiscountEntity> discountList) {
        discountList.forEach(item -> {
            item.getArea().forEach(area -> area.setId(saveItem(this.locations, area, Objects::equals).getId()));
            item.getCategories().forEach(category -> category.setId(saveItem(this.categories, category, Objects::equals).getId()));
        });
    }

    @Test
    public void whenLoadCsvSuccess() {
        // given
        final List<DiscountEntity> discountList = getDiscountList();
        saveDiscountsParameters(discountList);
        final MultipartFile csvData = newMockMultipartFile(discountList);

        // when
        final List<String> result = this.csvDiscountLoaderService.loadDiscountsFromCsv(csvData, ";");

        // then
        result.forEach(item -> assertTrue(item.endsWith(": OK")));
    }

    @Test
    public void whenLoadCsvDiscountExists() {
        // given
        final List<DiscountEntity> discountList = this.discountRepository.saveAll(getDiscountList());
        final List<String> benchmark = new ArrayList<>(discountList.stream().map(x -> x.getId() + ": SKIP already exists").toList());
        final MockMultipartFile csvData = newMockMultipartFile(discountList);

        // when
        final List<String> result = this.csvDiscountLoaderService.loadDiscountsFromCsv(csvData, ";");

        // then
        assertEquals(discountList.size(), this.discountRepository.findAll().size());
        assertEquals(benchmark, result);
    }

    @Test
    public void whenLoadCsvWithDiscountDuplicate() {
        // given
        final List<DiscountEntity> discountList = getDiscountList();
        final List<String> benchmark = new ArrayList<>(discountList.stream().map(x -> x.getId() + ": OK").toList());
        benchmark.add(discountList.size() + ": SKIP already exists");
        discountList.add(discountList.get(discountList.size() - 1));
        saveDiscountsParameters(discountList);
        final MockMultipartFile csvData = newMockMultipartFile(discountList);
        final List<DiscountEntity> discountsAfterUpload;

        // when
        final List<String> result = this.csvDiscountLoaderService.loadDiscountsFromCsv(csvData, ";");

        // then
        discountsAfterUpload = this.discountRepository.findAll();
        assertEquals(discountList.size() - 1, discountsAfterUpload.size());
        assertEquals(benchmark, result);
        for (int i = 0; i < Math.min(discountList.size(), this.discounts.size()); i++)
            assertTrue(isDiscountsEquals(this.discounts.get(i), discountsAfterUpload.get(i)));
    }

    @Test
    public void whenLoadCsvFailLocationNotFound() {
        // given
        final List<DiscountEntity> discountList = getDiscountList();
        final Set<LocationEntity> locationToEdit = discountList.get(0).getArea();
        locationToEdit.iterator().next().setCity("Брест");
        final MockMultipartFile csvData = newMockMultipartFile(discountList);

        // when
        final List<String> result = this.csvDiscountLoaderService.loadDiscountsFromCsv(csvData, ";");

        // then
        assertEquals("1: City Брест was not found in database", result.get(0));
    }

    @Test
    public void whenLoadCsvFailIncorrectNumberOfDelimitedFields() {
        // given
        final List<DiscountEntity> discountList = getDiscountList();
        discountList.get(0).setType("0123456789;0123456789");
        final MockMultipartFile csvData = newMockMultipartFile(discountList);

        // when
        final List<String> result = this.csvDiscountLoaderService.loadDiscountsFromCsv(csvData, ";");

        // then
        assertEquals(result.get(0),"1: Number of delimited fields does not match header");
    }
}