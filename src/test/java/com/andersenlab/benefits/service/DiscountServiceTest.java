package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.*;
import com.andersenlab.benefits.repository.*;
import com.andersenlab.benefits.service.impl.DiscountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;

import static java.lang.Math.random;
import static java.sql.Timestamp.valueOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static com.andersenlab.benefits.repository.DiscountSpec.getLastAdded;
import static com.andersenlab.benefits.service.ServiceTestUtils.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "spring.main.lazy-initialization=true",
        classes = {DiscountService.class, DiscountServiceImpl.class})
class DiscountServiceTest {
    private final DiscountServiceImpl discountService;
    private final List<CompanyEntity> companies = new ArrayList<>();
    private final List<LocationEntity> locations = new ArrayList<>();
    private final List<CategoryEntity> categories = new ArrayList<>();
    private final List<DiscountEntity> discounts = new ArrayList<>();

    @MockBean
    private final DiscountRepository discountRepository;

    @MockBean
    private final LocationRepository locationRepository;

    @MockBean
    private final CategoryRepository categoryRepository;

    @MockBean
    private final CompanyRepository companyRepository;

    @Autowired
    public DiscountServiceTest(final DiscountServiceImpl discountService,
                               final DiscountRepository discountRepository,
                               final LocationRepository locationRepository,
                               final CategoryRepository categoryRepository,
                               final CompanyRepository companyRepository) {
        this.discountService = discountService;
        this.discountRepository = discountRepository;
        this.locationRepository = locationRepository;
        this.categoryRepository = categoryRepository;
        this.companyRepository = companyRepository;
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

    private DiscountEntity getDiscount(long num) {
        return new DiscountEntity(
                num,
                "Type" + num,
                "Description" + num,
                "Condition" + num,
                "Size" + num,
                DiscountType.DISCOUNT,
                valueOf("2022-01-01 00:00:00"),
                valueOf("2022-12-31 00:00:00"),
                "Image" + num,
                getLocationList(),
                getCategoryList(),
                getCompany());
    }

    private List<DiscountEntity> getDiscountList(int discountsCount) {
        List<DiscountEntity> result = new ArrayList<>();
        for (long i = 1; i <= discountsCount; i++) {
            result.add(getDiscount(i));
        }
        return result;
    }

    private boolean isDiscountsEquals(final DiscountEntity discount1, final DiscountEntity discount2) {
        if (discount1 == discount2) return true;
        if (null == discount1 || discount1.getClass() != discount2.getClass()) {
            return false;
        }
        return (
                discount1.getType().equals(discount2.getType()) &&
                        discount1.getDescription().equals(discount2.getDescription()) &&
                        discount1.getDiscount_condition().equals(discount2.getDiscount_condition()) &&
                        discount1.getSizeDiscount().equals(discount2.getSizeDiscount()) &&
                        discount1.getImageDiscount().equals(discount2.getImageDiscount()) &&
                        isCompaniesEquals(discount1.getCompany(), discount2.getCompany())
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
    public void ResetData() {
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

        when(this.discountRepository.save(any())).thenAnswer(invocation ->
                saveItem(this.discounts, invocation.getArgument(0), this::isDiscountsEquals));
        when(this.discountRepository.saveAll(anyList())).thenAnswer(invocation -> {
                List<DiscountEntity> discountsToSave = invocation.getArgument(0);
                discountsToSave.forEach(item -> saveItem(this.discounts, item, this::isDiscountsEquals));
                return discountsToSave;
        });
        when(this.discountRepository.findAll()).thenReturn(this.discounts);
        when(this.discountRepository.findById(anyLong())).thenAnswer(invocation ->
            this.discounts.stream().filter(discount ->
                Objects.equals(discount.getId(), invocation.getArgument(0))).findFirst());
        doAnswer(invocation -> discounts.remove(this.discounts.stream().filter(discount ->
                    Objects.equals(discount.getId(), invocation.getArgument(0))).findFirst().orElseThrow()))
            .when(this.discountRepository).deleteById(anyLong());
        when(this.discountRepository.findAll(any(Specification.class))).thenAnswer(invocation -> {
            Object parameters = invocation.getArgument(0);
            Field argument = parameters.getClass().getDeclaredField("arg$1");
            argument.setAccessible(true);
            Object location = argument.get(parameters);
            Field value = location.getClass().getDeclaredField("val$location");
            value.setAccessible(true);
            String mask = (value.get(location)).toString();
            List<DiscountEntity> result = new ArrayList<>(discounts.size());
            this.discounts.forEach(discount -> discount.getArea().stream()
                    .filter(area -> area.getCity().startsWith(mask)).findFirst()
                    .ifPresent(item -> result.add(discount)));
            return result;
        });
    }

    @Test
    public void whenFindAllSuccess() {
        // given
        final int listLength = 10;
        final List<DiscountEntity> discountList = this.discountRepository.saveAll(getDiscountList(listLength));

        //when-
        final List<Optional<DiscountEntity>> discountResult = this.discountService.findAllDiscounts();

        //then
        assertEquals(listLength, discountResult.size());
        for (int i = 0; i < listLength; i++)
            assertTrue(isDiscountsEquals(discountList.get(i), discountResult.get(i).orElseThrow()));
    }

    @Test
    public void whenFindByIdSuccess() {
        // given
        final int listLength = 10;
        final List<DiscountEntity> discountList = this.discountRepository.saveAll(getDiscountList(listLength));
        final DiscountEntity discount = discountList.get((int)(random() * (listLength - 1) + 1));

        //when
        Optional<DiscountEntity> foundDiscount = this.discountService.findByIdDiscount(discount.getId());

        //then
        assertEquals(discount, foundDiscount.orElseThrow());
        verify(this.discountRepository, times(1)).findById(discount.getId());
    }

    @Test
    public void whenFindByIdNotPresent() {
        // given
        final int listLength = 10;
        final List<DiscountEntity> discountList = this.discountRepository.saveAll(getDiscountList(listLength));
        final long id = discountList.get(listLength - 1).getId() + 1;

        //when
        Optional<DiscountEntity> foundDiscount = this.discountService.findByIdDiscount(id);

        //then
        assertEquals(Optional.empty(), foundDiscount);
        verify(this.discountRepository, times(1)).findById(id);
    }

    @Test
    public void whenCreateDiscountSuccess() {
        // given
        final int listLength = 5;
        final List<DiscountEntity> discountList = this.discountRepository.saveAll(getDiscountList(listLength));
        final DiscountEntity newDiscount = getDiscount(discountList.get(listLength - 1).getId() + 1);

        //when
        final Optional<DiscountEntity> discountSaved = this.discountService.createDiscount(newDiscount);

        //then
        assertTrue(isDiscountsEquals(newDiscount, discountSaved.orElseThrow()));
        verify(this.discountRepository, times(1)).save(newDiscount);
    }

    @Test
    public void whenUpdateDiscountSuccess() {
        // given
        final int listLength = 5;
        final List<DiscountEntity> discountList = this.discountRepository.saveAll(getDiscountList(listLength));
        final DiscountEntity discountToUpdate = discountList.get((int)(random() * (listLength - 1) + 1));
        discountToUpdate.setDiscount_type(DiscountType.GIFT);
        discountToUpdate.setDescription("newDescription");

        //when
        final DiscountEntity discountUpdated = this.discountService
                .updateDiscountById(discountToUpdate.getId(), discountToUpdate).orElseThrow();

        //then
        assertTrue(isDiscountsEquals(discountToUpdate, discountUpdated));
    }

    @Test
    public void whenDeleteDiscountSuccess() {
        // given
        final int listLength = 10;
        final List<DiscountEntity> discountList = this.discountRepository.saveAll(getDiscountList(listLength));
        final DiscountEntity discountToDelete = discountList.get((int)(random() * (listLength - 1) + 1));

        // when
        this.discountService.deleteDiscountById(discountToDelete.getId());

        //then
        assertEquals(listLength - 1, discountRepository.findAll().size());
        assertEquals(Optional.empty(), discountRepository.findById(discountToDelete.getId()));
        verify(this.discountRepository, times(1)).deleteById(discountToDelete.getId());
    }

    @Test
    public void whenFindWithCriteria() {
        // given
        final int listLength = 10;
        this.discountRepository.saveAll(getDiscountList(listLength));
        final Specification<DiscountEntity> spec = Specification.where(
                DiscountSpec.getByLocation("City").and(getLastAdded()));

        // when
        final List<DiscountEntity> foundDiscounts = discountService.getDiscountsByCriteria(spec);

        // then
        assertEquals(listLength, foundDiscounts.size());
    }

    @Test
    public void whenFindWithCriteriaEmptyResponse() {
        // given
        final int listLength = 10;
        this.discountRepository.saveAll(getDiscountList(listLength));
        final Specification<DiscountEntity> spec = Specification.where(
                DiscountSpec.getByLocation("unknownCity").and(getLastAdded()));

        // when
        final List<DiscountEntity> foundDiscounts = discountService.getDiscountsByCriteria(spec);

        // then
        assertEquals(0, foundDiscounts.size());
    }
}
