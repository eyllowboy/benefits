package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.domain.DiscountType;

import com.andersenlab.benefits.repository.DiscountRepository;
import com.andersenlab.benefits.repository.DiscountSpec;
import com.andersenlab.benefits.service.impl.DiscountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

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
    private final List<DiscountEntity> discounts = new ArrayList<>();

    @MockBean
    private final DiscountRepository discountRepository;
    @MockBean
    private final CompanyService companyService;
    @MockBean
    private final LocationService locationService;
    @MockBean
    private final CategoryService categoryService;

    @Autowired
    public DiscountServiceTest(final DiscountServiceImpl discountService,
                               final DiscountRepository discountRepository,
                               final CompanyService companyService,
                               final LocationService locationService,
                               final CategoryService categoryService) {
        this.discountService = discountService;
        this.discountRepository = discountRepository;
        this.companyService = companyService;
        this.locationService = locationService;
        this.categoryService = categoryService;
    }

    @BeforeEach
    public void ResetData() {
        this.discounts.clear();

        when(this.discountRepository.save(any())).thenAnswer(invocation ->
                saveItem(this.discounts, invocation.getArgument(0), ServiceTestUtils::isDiscountsEquals));
        when(this.discountRepository.saveAll(anyList())).thenAnswer(invocation -> {
            final List<DiscountEntity> discountsToSave = invocation.getArgument(0);
            discountsToSave.forEach(item -> saveItem(this.discounts, item, ServiceTestUtils::isDiscountsEquals));
            return discountsToSave;
        });
        when(this.discountRepository.findAll()).thenReturn(this.discounts);
        when(this.discountRepository.findById(anyLong())).thenAnswer(invocation ->
                this.discounts.stream().filter(discount ->
                        Objects.equals(discount.getId(), invocation.getArgument(0))).findFirst());
        doAnswer(invocation -> this.discounts.remove((DiscountEntity) invocation.getArgument(0)))
                .when(this.discountRepository).delete(any(DiscountEntity.class));
        when(this.discountRepository.findAll(any(Specification.class))).thenAnswer(invocation -> {
            final Object parameters = invocation.getArgument(0);
            final Field argument = parameters.getClass().getDeclaredField("arg$1");
            argument.setAccessible(true);
            final Object location = argument.get(parameters);
            final Field value = location.getClass().getDeclaredField("val$location");
            value.setAccessible(true);
            final String mask = (value.get(location)).toString();
            final List<DiscountEntity> result = new ArrayList<>(this.discounts.size());
            this.discounts.forEach(discount -> discount.getArea().stream()
                    .filter(area -> area.getCity().startsWith(mask)).findFirst()
                    .ifPresent(item -> result.add(discount)));
            return result;
        });
    }

    @Test
    public void whenFindAllSuccess() {
        // given
        final List<DiscountEntity> discountList = this.discountRepository.saveAll(getDiscountList());
        final Page<DiscountEntity> pageOfDiscount = new PageImpl<>(discountList);

        // when
        when(this.discountRepository.findAll(PageRequest.of(0,10))).thenReturn(pageOfDiscount);
        final Page<DiscountEntity> foundCategory = this.discountRepository.findAll(PageRequest.of(0,10));

        // then
        assertEquals(pageOfDiscount, foundCategory);
        verify(this.discountRepository, times(1)).findAll(PageRequest.of(0,10));
    }

    @Test
    public void whenFindByIdSuccess() {
        // given
        final List<DiscountEntity> discountList = this.discountRepository.saveAll(getDiscountList());
        final DiscountEntity discount = discountList.get(getRndEntityPos());

        // when
        final DiscountEntity foundDiscount = this.discountService.findById(discount.getId());

        // then
        assertEquals(discount, foundDiscount);
        verify(this.discountRepository, times(1)).findById(discount.getId());
    }

    @Test()
    public void whenFindByIdNotPresent() {
        // given
        final List<DiscountEntity> discountList = this.discountRepository.saveAll(getDiscountList());
        final long id = discountList.get(discountList.size() - 1).getId() + 1;

        // when
        final Throwable thrown = assertThrows(IllegalStateException.class, () ->
                this.discountService.findById(id));

        // then
        assertNotNull(thrown.getMessage());
        verify(this.discountRepository, times(1)).findById(id);
    }

    @Test
    public void whenCreateDiscountSuccess() {
        // given
        final List<DiscountEntity> discountList = this.discountRepository.saveAll(getDiscountList());
        final DiscountEntity newDiscount = getDiscount(discountList.get(discountList.size() - 1).getId() + 1);

        // when
        final DiscountEntity discountSaved = this.discountService.save(newDiscount);

        // then
        assertTrue(isDiscountsEquals(newDiscount, discountSaved));
        verify(this.discountRepository, times(1)).save(newDiscount);
    }

    @Test
    public void whenUpdateDiscountSuccess() {
        // given
        final List<DiscountEntity> discountList = this.discountRepository.saveAll(getDiscountList());
        final DiscountEntity discountToUpdate = discountList.get(getRndEntityPos());
        discountToUpdate.setDiscount_type(DiscountType.GIFT);
        discountToUpdate.setDescription("newDescription");

        // when
        final DiscountEntity discountUpdated = this.discountService.update(discountToUpdate.getId(), discountToUpdate);

        // then
        assertTrue(isDiscountsEquals(discountToUpdate, discountUpdated));
    }

    @Test
    public void whenDeleteDiscountSuccess() {
        // given
        final List<DiscountEntity> discountList = this.discountRepository.saveAll(getDiscountList());
        final DiscountEntity discountToDelete = discountList.get(getRndEntityPos());

        // when
        this.discountService.delete(discountToDelete.getId());

        // then
        assertEquals(discountList.size() - 1, this.discountRepository.findAll().size());
        assertEquals(Optional.empty(), this.discountRepository.findById(discountToDelete.getId()));
        verify(this.discountRepository, times(1)).delete(discountToDelete);
    }

    @Test
    public void whenFindWithCriteria() {
        // given
        final List<DiscountEntity> discountEntities = this.discountRepository.saveAll(getDiscountList());
        final Page<DiscountEntity> pageOfDiscounts = new PageImpl<>(discountEntities);
        final Specification<DiscountEntity> spec = Specification.where(
                DiscountSpec.getByLocation("City").and(getLastAdded()));
        when(this.discountRepository.findAll(spec, PageRequest.of(0, 10))).thenReturn(pageOfDiscounts);

        // when
        final Page<DiscountEntity> foundDiscounts = this.discountService.getDiscountsByCriteria(spec,PageRequest.of(0,10));

        // then
        assertEquals(pageOfDiscounts, foundDiscounts);
        verify(this.discountRepository, times(1)).findAll(spec, PageRequest.of(0, 10));
    }

    @Test
    public void whenFindWithCriteriaEmptyResponse() {
        // given
        final List<DiscountEntity> listOfDiscount = List.of(new DiscountEntity());
        final Page<DiscountEntity> pageOfDiscounts = new PageImpl<>(listOfDiscount);
        final Specification<DiscountEntity> spec = Specification.where(
                DiscountSpec.getByLocation("unknownCity").and(getLastAdded()));

        // when
        when(this.discountRepository.findAll(spec, PageRequest.of(0, 10))).thenReturn(pageOfDiscounts);
        final Page<DiscountEntity> foundDiscounts = this.discountService.getDiscountsByCriteria(spec,PageRequest.of(0,10));

        // then
        assertEquals(1, foundDiscounts.getTotalElements());
        verify(this.discountRepository, times(1)).findAll(spec,PageRequest.of(0, 10));
    }
}
