package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.Discount;
import com.andersenlab.benefits.repository.DiscountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private DiscountRepository discountRepository;

    @Autowired
    private DiscountService discountService;


    @BeforeEach
    void setUp() {
        discountService = new DiscountService(discountRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void findByIdDiscount() {
        Discount discount = new Discount(1, 2, 3, "title", "description", 20, new Date(12022020), new Date(12032020), 1, "image");
        //when(discountRepository.findById(1)).thenReturn(Optional.of(discount));
        //assertEquals(1, discountService.findByIdDiscount(1).getId());

        //int id = 1;
        //Discount discount = discountService.findByIdDiscount(id);
        //assertThat(discount.getId()).isEqualTo(1);

        //int id=1;
        //when(discountRepository.findById(any())).thenReturn(Optional.of(discount));
        //Discount actual = discountService.findByIdDiscount(id);
        //assertEquals(discount, actual);
        //verify(discountRepository).findById(any());

    }

    @Test
    void shouldFindAllDiscounts() {
        discountService.findAllDiscounts();

        verify(discountRepository).findAll();
    }

    @Test
    void createDiscount() {
        Discount discount = new Discount(1, 2, 3, "title", "description", 20, new Date(12022020), new Date(12032020), 1, "image");

        discountService.createDiscount(discount);

        ArgumentCaptor<Discount> discountArgumentCaptor = ArgumentCaptor.forClass(Discount.class);

        verify(discountRepository).save(discountArgumentCaptor.capture());

        Discount captureDiscount = discountArgumentCaptor.getValue();

        assertThat(captureDiscount).isEqualTo(discount);
    }

    @Test
    @Disabled
    void updateDiscountById() {
    }

    @Test
    @Disabled
    void deleteDiscountById() {
    }
}