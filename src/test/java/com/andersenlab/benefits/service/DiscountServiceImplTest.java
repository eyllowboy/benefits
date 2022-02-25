package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.*;
import com.andersenlab.benefits.repository.CategoryRepository;
import com.andersenlab.benefits.repository.CompanyRepository;
import com.andersenlab.benefits.repository.DiscountRepository;
import com.andersenlab.benefits.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static java.sql.Timestamp.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceImplTest {

    @Mock
    private DiscountRepository discountRepository;

    private DiscountServiceImpl discountService;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CompanyRepository companyRepository;

    @BeforeEach
    private void setUp() {
        discountService = new DiscountServiceImpl(discountRepository);

    }

    @Test
    public void whenFindByIdReturnDiscount() {
        // given
        final Set<CategoryEntity> categories = new HashSet<>();
        categories.add(categoryRepository.getById(1L));
        final Set<LocationEntity> locations = new HashSet<>();
        locations.add(locationRepository.getById(1L));
        final CompanyEntity company = companyRepository.getById(3L);
        final DiscountEntity discount = new DiscountEntity(6L, "cool", "desc", "condition", "20", DiscountType.DISCOUNT, valueOf("2022-01-20 15:34:23"), valueOf("2022-01-20 15:34:23"), "image", categories, locations, company);
        //when
        when(discountService.findByIdDiscount(1L)).thenReturn(Optional.of(discount));
        Optional<DiscountEntity> actual = discountService.findByIdDiscount(1L);
        //then
        assertEquals(Optional.of(discount), actual);
        verify(discountRepository).findById(1L);
    }

    @Test
    public void whenFindByIdDiscountIsNotPresent() {
        //when
        final Optional<DiscountEntity> discount = discountService.findByIdDiscount(1L);
        //then
        assertEquals(discount, Optional.empty());
        verify(discountRepository).findById(1L);

    }


    @Test
    public void whenFindAllDiscounts() {
        // given
        final Set<CategoryEntity> categories = new HashSet<>();
        categories.add(categoryRepository.getById(1L));
        final Set<LocationEntity> locations = new HashSet<>();
        locations.add(locationRepository.getById(1L));
        final List<DiscountEntity> listDiscounts = new ArrayList<>();
        final CompanyEntity company = companyRepository.getById(3L);
        for (int i = 1; i < 6; i++) {
            listDiscounts.add(new DiscountEntity(2L, "cool", "desc", "condition", "20", DiscountType.DISCOUNT, valueOf("2022-01-20 15:34:23"), valueOf("2022-01-20 15:34:23"), "image", categories, locations, company));
        }
        //when
        when(discountRepository.findAll()).thenReturn(listDiscounts);
        final List<DiscountEntity> discountList = discountService.findAllDiscounts().stream().map(Optional::orElseThrow).toList();
        //then
        assertEquals(listDiscounts, discountList);
        verify(discountRepository).findAll();
    }


    @Test
    public void whenCreateDiscountIsOk() {
        // given
        final Set<CategoryEntity> categories = new HashSet<>();
        categories.add(categoryRepository.getById(1L));
        final Set<LocationEntity> locations = new HashSet<>();
        locations.add(locationRepository.getById(1L));
        final CompanyEntity company = companyRepository.getById(3L);
        final DiscountEntity discount = new DiscountEntity(6L, "cool", "desc", "condition", "20", DiscountType.DISCOUNT, valueOf("2022-01-20 15:34:23"), valueOf("2022-01-20 15:34:23"), "image", categories, locations, company);
        //when
        when(discountRepository.save(any())).thenReturn(discount);
        final Optional<DiscountEntity> discountSaved = discountService.createDiscount(discount);
        //then
        assertEquals(Optional.of(discount), discountSaved);
        verify(discountRepository, times(1)).save(discount);
    }

    @Test
    public void whenUpdateDiscountByIdIsOk() {
        // given
        final Set<CategoryEntity> categories = new HashSet<>();
        categories.add(categoryRepository.getById(1L));
        final Set<LocationEntity> locations = new HashSet<>();
        locations.add(locationRepository.getById(1L));
        final CompanyEntity company = companyRepository.getById(3L);
        final DiscountEntity oldDiscount = new DiscountEntity(6L, "cool", "desc", "condition", "20", DiscountType.DISCOUNT, valueOf("2022-01-20 15:34:23"), valueOf("2022-01-20 15:34:23"), "image", categories, locations, company);
        final DiscountEntity newDiscount = new DiscountEntity();
        newDiscount.setType("title2");
        oldDiscount.setType(newDiscount.getType());
        //when
        when(discountRepository.findById(any())).thenReturn(Optional.of(oldDiscount));
        when(discountRepository.save(any())).thenReturn(oldDiscount);
        discountService.createDiscount(oldDiscount);
        discountService.updateDiscountById(oldDiscount.getId(), oldDiscount);
        final Optional<DiscountEntity> discountUpdated = discountService.findByIdDiscount(1L);
        //then
        assertEquals("title2", discountUpdated.get().getType());

    }

    @Test
    public void whenDeleteDiscountByIdIsOk() {
        //when
        discountService.deleteDiscountById(anyLong());
        //then
        verify(discountRepository).deleteById(anyLong());

    }
}
