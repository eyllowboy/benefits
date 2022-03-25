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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.lang.Math.random;
import static java.sql.Timestamp.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "spring.main.lazy-initialization=true",
        classes = {CsvDiscountLoaderService.class, CsvDiscountLoaderServiceImpl.class})
public class CsvDiscountLoaderServiceTest {

    CsvDiscountLoaderServiceImpl csvDiscountLoaderService;
    List<CompanyEntity> companies = new ArrayList<>();
    List<LocationEntity> locations = new ArrayList<>();
    List<CategoryEntity> categories = new ArrayList<>();
    List<DiscountEntity> discounts = new ArrayList<>();

    @MockBean
    DiscountRepository discountRepository;

    @MockBean
    CompanyRepository companyRepository;

    @MockBean
    CsvDiscountLoaderRepository csvDiscountLoaderRepository;

    @MockBean
    CategoryRepository categoryRepository;

    @MockBean
    LocationRepository locationRepository;

    @Autowired
    public CsvDiscountLoaderServiceTest(CsvDiscountLoaderServiceImpl csvDiscountLoaderService) {
        this.csvDiscountLoaderService = csvDiscountLoaderService;
    }

    private Set<CategoryEntity> getCategoryList() {
        Set<CategoryEntity> result = new LinkedHashSet<>();
        final int size = (int) (random() * 10 + 1);
        for (long i = 1; i <= size; i++) {
            CategoryEntity category = this.categoryRepository.findByTitle("Category" + i)
                    .orElse(new CategoryEntity("Category" + i));
            if (null == category.getId())
                categoryRepository.save(category);
            result.add(new CategoryEntity(category.getId(), category.getTitle(), category.getDiscounts()));
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
            result.add(new LocationEntity(location.getId(), location.getCountry(), location.getCity(), location.getDiscounts()));
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

    private List<DiscountEntity> getDiscountList(int discountsCount) {
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

    private String discountToString(DiscountEntity discount) {
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

    private MockMultipartFile newMockMultipartFile(List<DiscountEntity> discounts) {
        StringBuilder contents = new StringBuilder("number;company_title;type;category;image;company_description;company_address;company_phone;links;size;discount_type;discount_description;discount_condition;start_date;end_date;location");
        discounts.forEach(discount -> contents.append("\n").append(discountToString(discount)));
        return (new MockMultipartFile(
                "file",
                "discounts.csv",
                "multipart/form-data",
                contents.toString().getBytes(StandardCharsets.UTF_8)));
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

    private boolean isCompaniesEquals(final CompanyEntity company1, final CompanyEntity company2) {
        return (
                company1.getTitle().equals(company2.getTitle()) &&
                company1.getAddress().equals(company2.getAddress()) &&
                company1.getDescription().equals(company2.getDescription()) &&
                company1.getPhone().equals(company2.getPhone()) &&
                company1.getLink().equals(company2.getLink())
        );
    }

    private <E> E saveItem(final Collection<E> collection, final E item, final BiFunction<E, E, Boolean> compareMethod) {
        final E result = collection.stream().filter(element -> compareMethod.apply(item, element)).findFirst().orElse(item);
        if (result == item) {
            collection.add(item);
            try {
                Method setId = Arrays.stream(item.getClass().getMethods()).filter(method ->
                        Objects.equals(method.getName(), "setId")).findFirst().orElse(null);
                if (null != setId)
                    setId.invoke(item, (long) collection.size());
            } catch (InvocationTargetException | IllegalAccessException ex) {
                return result;
            }
        }
        return result;
    }

    @BeforeEach
    private void ResetData() {
        this.companies.clear();
        this.locations.clear();
        this.categories.clear();
        this.discounts.clear();

        when(this.companyRepository.save(any())).thenAnswer(invocation ->
                    saveItem(this.companies, invocation.getArgument(0), this::isCompaniesEquals));

        when(this.locationRepository.save(any())).thenAnswer(invocation ->
                saveItem(this.locations, invocation.getArgument(0), Objects::equals));

        when(this.categoryRepository.save(any())).thenAnswer(invocation ->
                saveItem(this.categories, invocation.getArgument(0), Objects::equals));
        when(this.categoryRepository.findByTitle(any())).thenAnswer(invocation -> {
            CategoryEntity result = this.categories.stream().filter(category ->
                    Objects.equals(category.getTitle(), invocation.getArgument(0))).findFirst().orElse(null);
            return result != null ? Optional.of(result) : Optional.empty();
        });

        when(this.discountRepository.save(any())).thenAnswer(invocation ->
                saveItem(this.discounts, invocation.getArgument(0), this::isDiscountsEquals));
        when(this.discountRepository.findAll()).thenReturn(this.discounts);

        when(this.csvDiscountLoaderRepository.findLocationByCity(any())).thenAnswer(invocation -> {
            if (locations.size() == 0)
                return Optional.empty();
            LocationEntity result = this.locations.stream().filter(location ->
                Objects.equals(location.getCity(), invocation.getArgument(0))).findFirst().orElse(null);
            return null != result ? Optional.of(result) : Optional.empty();
        });
        when(this.csvDiscountLoaderRepository.findCompanyByTitle(any())).thenAnswer(invocation -> {
            CompanyEntity result = this.companies.stream().filter(company ->
                    Objects.equals(company.getTitle(), invocation.getArgument(0))).findFirst().orElse(null);
            return null != result ? Optional.of(result) : Optional.empty();
        });
        when(this.csvDiscountLoaderRepository.findDiscountByCompanyId(any())).thenAnswer(invocation ->
            discounts.stream().filter(discount ->
                isCompaniesEquals(discount.getCompany_id(), invocation.getArgument(0))).toList());
    }

    @Test
    public void whenLoadCsvSuccess() {
        // given
        final int discountsCount = 10;
        final List<DiscountEntity> discountList = getDiscountList(discountsCount);
        final MultipartFile csvData = newMockMultipartFile(discountList);

        // when
        final List<String> result = this.csvDiscountLoaderService.loadDiscountsFromCsv(csvData, ";");

        // then
        for (int i = 0; i < discountsCount; i++)
            assertEquals((i + 1 ) + ": OK", result.get(i));
    }

    @Test
    public void whenLoadCsvDiscountExists() {
        // given
        final int discountsCount = 20;
        final List<DiscountEntity> discountList = getDiscountList(discountsCount);
        final List<String> benchmark = new ArrayList<>();
        discountList.forEach(discount -> {
            CompanyEntity company = this.csvDiscountLoaderRepository
                    .findCompanyByTitle(discount.getCompany_id().getTitle())
                    .orElse(discount.getCompany_id());
            if (null == company.getId())
                this.companyRepository.save(company);
            discount.setCompany_id(company);
            this.discountRepository.save(discount);
            benchmark.add(discount.getId() + ": SKIP already exists");
        });
        final MockMultipartFile csvData = newMockMultipartFile(discountList);

        // when
        final List<String> result = this.csvDiscountLoaderService.loadDiscountsFromCsv(csvData, ";");

        // then
        assertEquals(discountsCount, this.discountRepository.findAll().size());
        assertEquals(benchmark, result);
    }

    @Test
    public void whenLoadCsvWithDiscountDuplicate() {
        // given
        int discountsCount = 3;
        final List<DiscountEntity> discountList = getDiscountList(discountsCount);
        final List<String> benchmark = new ArrayList<>();
        discountList.forEach(discount -> benchmark.add(discount.getId() + ": OK"));
        discountList.add(discountList.get(discountsCount - 1));
        benchmark.add(discountsCount + ": SKIP already exists");
        final MockMultipartFile csvData = newMockMultipartFile(discountList);
        final List<DiscountEntity> discountsAfterUpload;

        // when
        final List<String> result = this.csvDiscountLoaderService.loadDiscountsFromCsv(csvData, ";");

        // then
        discountsAfterUpload = this.discountRepository.findAll();
        assertEquals(discountsCount, discountsAfterUpload.size());
        assertEquals(benchmark, result);
        for (int i = 0; i < discountsCount; i++)
            assertTrue(isDiscountsEquals(discounts.get(i), discountsAfterUpload.get(i)));
    }

    @Test
    public void whenLoadCsvFailLocationNotFound() {
        // given
        int discountsCount = 1;
        final List<DiscountEntity> discountList = getDiscountList(discountsCount);
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
        int discountsCount = 10;
        final List<DiscountEntity> discountList = getDiscountList(discountsCount);
        discountList.get(0).setType("0123456789;0123456789");
        final MockMultipartFile csvData = newMockMultipartFile(discountList);

        // when
        final List<String> result = this.csvDiscountLoaderService.loadDiscountsFromCsv(csvData, ";");

        // then
        assertEquals(result.get(0),"1: Number of delimited fields does not match header");
    }
}