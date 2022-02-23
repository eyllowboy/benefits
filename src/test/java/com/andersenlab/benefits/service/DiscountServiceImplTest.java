package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.CategoryEntity;
import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.repository.CategoryRepository;
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
class DiscountServiceTest {

    @Mock
    private DiscountRepository discountRepository;

    private DiscountServiceImpl discountService;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private CategoryRepository categoryRepository;

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
        final DiscountEntity discount = new DiscountEntity(6L, categories, 3L, "title6", "description", "no condition",
                "20", new Date(12022020), new Date(12022020), locations, "image");
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
        final List<DiscountEntity> listDiscounts = List.of(
            new DiscountEntity(1L, categories, 1L, "title1", "description", "no condition",
                "20", new Date(12022020), new Date(12022020), locations, "image"),
            new DiscountEntity(2L, categories, 2L, "title2", "description", "no condition",
                    "20", new Date(12022020), new Date(12022020), locations, "image"),
            new DiscountEntity(3L, categories, 3L, "title3", "description", "no condition",
                    "20", new Date(12022020), new Date(12022020), locations, "image"),
            new DiscountEntity(4L, categories, 4L, "title4", "description", "no condition",
                    "20", new Date(12022020), new Date(12022020), locations, "image"),
            new DiscountEntity(5L, categories, 5L, "title5", "description", "no condition",
                    "20", new Date(12022020), new Date(12022020), locations, "image"),
            new DiscountEntity(6L, categories, 6L, "title6", "description", "no condition",
                    "20", new Date(12022020), new Date(12022020), locations, "image"));
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
        final DiscountEntity discount = new DiscountEntity(6L, categories, 3L, "title6", "description", "no condition",
                "20", new Date(12022020), new Date(12022020), locations, "image");
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
        final DiscountEntity oldDiscount = new DiscountEntity(6L, categories, 3L, "title6", "description", "no condition",
                "20", new Date(12022020), new Date(12022020), locations, "image");
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
