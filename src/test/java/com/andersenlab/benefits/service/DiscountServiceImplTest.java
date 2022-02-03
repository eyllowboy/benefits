package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.Discount;
import com.andersenlab.benefits.repository.DiscountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.sql.Timestamp.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private DiscountRepository discountRepository;

    private DiscountServiceImpl discountService;

    @BeforeEach
    private void setUp() {
        discountService = new DiscountServiceImpl(discountRepository);

    }

    @Test
    public void whenFindByIdReturnDiscount() {
        //given
        final Discount discount = new Discount(1L, 2L, 3L, "title", "description", 20, new Date(12022020), new Date(12032020), 1L, "image");
        //when
        when(discountService.findByIdDiscount(1L)).thenReturn(Optional.of(discount));
        Optional<Discount> actual = discountService.findByIdDiscount(1L);
        //then
        assertEquals(Optional.of(discount), actual);
        verify(discountRepository).findById(1L);
    }

    @Test
    public void whenFindByIdDiscountIsNotPresent() {
        //when
        final Optional<Discount> discount = discountService.findByIdDiscount(1L);
        //then
        assertEquals(discount, Optional.empty());
        verify(discountRepository).findById(1L);

    }


    @Test
    public void whenFindAllDiscounts() {
        //given
        final List<Discount> listDiscounts = List.of(
                new Discount(1L, 2L, 6L, "title1", "description", 20, valueOf("2022-01-20 15:34:23"), valueOf("2022-01-20 15:34:23"), 1L, "image"),
                new Discount(2L, 3L, 2L, "title2", "description", 20, valueOf("2022-01-20 15:34:23"), valueOf("2022-01-20 15:34:23"), 1L, "image"),
                new Discount(3L, 1L, 3L, "title3", "description", 20, valueOf("2022-01-20 15:34:23"), valueOf("2022-01-20 15:34:23"), 1L, "image"),
                new Discount(4L, 5L, 1L, "title3", "description", 20, valueOf("2022-01-20 15:34:23"), valueOf("2022-01-20 15:34:23"), 1L, "image"),
                new Discount(5L, 5L, 3L, "title4", "description", 20, valueOf("2022-01-20 15:34:23"), valueOf("2022-01-20 15:34:23"), 1L, "image")
        );
        //when
        when(discountRepository.findAll()).thenReturn(listDiscounts);
        final List<Discount> discountList = discountService.findAllDiscounts().stream().map(Optional::orElseThrow).toList();
        //then
        assertEquals(listDiscounts, discountList);
        verify(discountRepository).findAll();
    }


    @Test
    public void whenCreateDiscountIsOk() {
        //given
        final Discount discount = new Discount(1L, 2L, 3L, "title", "description", 20, valueOf("2022-01-20 15:34:23"), valueOf("2022-01-20 15:34:23"), 1L, "image");
        //when
        when(discountRepository.save(any())).thenReturn(discount);
        final Optional<Discount> discountSaved = discountService.createDiscount(discount);
        //then
        assertEquals(Optional.of(discount), discountSaved);
        verify(discountRepository, times(1)).save(discount);
    }

    @Test
    public void whenUpdateDiscountByIdIsOk() {
        //given
        final Discount oldDiscount = new Discount(1L, 2L, 3L, "title", "description", 20, valueOf("2022-01-20 15:34:23"), valueOf("2022-01-20 15:34:23"), 1L, "image");
        final Discount newDiscount = new Discount();
        newDiscount.setTitle("title2");
        oldDiscount.setTitle(newDiscount.getTitle());
        //when
        when(discountRepository.findById(any())).thenReturn(Optional.of(oldDiscount));
        when(discountRepository.save(any())).thenReturn(oldDiscount);
        discountService.createDiscount(oldDiscount);
        discountService.updateDiscountById(oldDiscount.getId(), oldDiscount);
        final Optional<Discount> discountUpdated = discountService.findByIdDiscount(1L);
        //then
        assertEquals("title2", discountUpdated.get().getTitle());

    }

    @Test
    public void whenDeleteDiscountByIdIsOk() {
        //when
        discountService.deleteDiscountById(anyLong());
        //then
        verify(discountRepository).deleteById(anyLong());

    }


}
