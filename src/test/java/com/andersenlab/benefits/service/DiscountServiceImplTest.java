package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.Discount;
import com.andersenlab.benefits.repository.DiscountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private DiscountRepository discountRepository;

    private DiscountServiceImpl discountService;


    @BeforeEach
    void setUp() {
        discountService = new DiscountServiceImpl(discountRepository);

    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void shouldFindByIdDiscount() {
        Discount discount = new Discount(1L, 2L, 3L, "title", "description", 20, new Date(12022020), new Date(12032020), 1, "image");
        when(discountService.findByIdDiscount(1L)).thenReturn(Optional.of(discount));
        Optional<Discount> actual = discountService.findByIdDiscount(1L);
        assertEquals(Optional.of(discount), actual);
        verify(discountRepository).findById(1L);
    }

    @Test
    void shouldFindByIdDiscountIsNotPresent() {
        Optional<Discount> discount = discountService.findByIdDiscount(1L);
        assertEquals(discount, Optional.empty());
    }


    @Test
    void shouldFindAllDiscounts() {
        List<Discount> listDiscounts = List.of(
                new Discount(1L, 2L, 6L, "title1", "description", 20, new Date(12022020), new Date(12032020), 1, "image"),
                new Discount(2L, 3L, 2L, "title2", "description", 20, new Date(12022020), new Date(12032020), 1, "image"),
                new Discount(3L, 1L, 3L, "title3", "description", 20, new Date(12022020), new Date(12032020), 1, "image"),
                new Discount(4L, 5L, 1L, "title3", "description", 20, new Date(12022020), new Date(12032020), 1, "image"),
                new Discount(5L, 5L, 3L, "title4", "description", 20, new Date(12022020), new Date(12032020), 1, "image")
        );
        when(discountRepository.findAll()).thenReturn(listDiscounts);
        List<Discount> discountList = discountService.findAllDiscounts().stream().map(Optional::orElseThrow).toList();
        assertEquals(listDiscounts, discountList);
        verify(discountRepository).findAll();
    }


    @Test
    void createDiscount() {
        Discount discount = new Discount(1L, 2L, 3L, "title", "description", 20, new Date(12022020), new Date(12032020), 1, "image");
        when(discountRepository.save(any())).thenReturn(discount);
        Optional<Discount> discountSaved = discountService.createDiscount(discount);
        assertEquals(Optional.of(discount), discountSaved);
        verify(discountRepository, times(1)).save(discount);
    }

    @Test
    void updateDiscountById() {
        Discount oldDiscount = new Discount(1L, 2L, 3L, "title", "description", 20, new Date(12022020), new Date(12032020), 1, "image");
        when(discountRepository.save(any())).thenReturn(oldDiscount);
        discountService.createDiscount(oldDiscount);
        given(discountRepository.findById(oldDiscount.getId())).willReturn(Optional.of(oldDiscount));
        Discount newDiscount = new Discount();
        newDiscount.setTitle("title2");
        oldDiscount.setTitle(newDiscount.getTitle());
        discountService.updateDiscountById(oldDiscount.getId(), newDiscount);

        Optional<Discount> discountUpdated = discountService.findByIdDiscount(1L);

        assertEquals("title2", discountUpdated.get().getTitle());

    }

    @Test
    void deleteDiscountById() {
        Discount discount = new Discount(1L, 2L, 3L, "title", "description", 20, new Date(12022020), new Date(12032020), 1, "image");
        discountService.deleteDiscountById(1L);
        Optional<Discount> discount1 = discountService.findByIdDiscount(1L);
        assertThat(discount1.isEmpty());
        verify(discountRepository).deleteById(1L);

    }


}
