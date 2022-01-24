package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.Discount;
import com.andersenlab.benefits.repository.DiscountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscountServiceImplTest {

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
        discountService.createDiscount(discount);
        when(discountService.findByIdDiscount(1L)).thenReturn(Optional.of(discount));
        Optional<Discount> actual = discountService.findByIdDiscount(1L);
        assertEquals(Optional.of(discount), actual);

    }


    @Test
    void shouldFindByIdDiscountIsNotPresent() {
        Optional<Discount> discount = discountService.findByIdDiscount(1L);
        assertEquals(discount, Optional.empty());

    }


    @Test
    void shouldFindAllDiscounts() {
        List<Discount> listDiscounts = new ArrayList<>();
        Discount oldDiscount1 = new Discount(1L, 2L, 6L, "title1", "description", 20, new Date(12022020), new Date(12032020), 1, "image");
        Discount oldDiscount2 = new Discount(2L, 3L, 2L, "title2", "description", 20, new Date(12022020), new Date(12032020), 1, "image");
        Discount oldDiscount3 = new Discount(3L, 1L, 3L, "title3", "description", 20, new Date(12022020), new Date(12032020), 1, "image");
        Discount oldDiscount4 = new Discount(4L, 5L, 1L, "title3", "description", 20, new Date(12022020), new Date(12032020), 1, "image");
        Discount oldDiscount5 = new Discount(5L, 5L, 3L, "title4", "description", 20, new Date(12022020), new Date(12032020), 1, "image");
        listDiscounts.add(oldDiscount1);
        listDiscounts.add(oldDiscount2);
        listDiscounts.add(oldDiscount3);
        listDiscounts.add(oldDiscount4);
        listDiscounts.add(oldDiscount5);
        when(discountRepository.findAll()).thenReturn(listDiscounts);
        List<Discount> discountList = discountService.findAllDiscounts();
        verify(discountRepository).findAll();
        assertEquals(5, discountList.size());
    }

    @Test
    void createDiscount() {
        Discount discount = new Discount(1L, 2L, 3L, "title", "description", 20, new Date(12022020), new Date(12032020), 1, "image");
        discountService.createDiscount(discount);
        ArgumentCaptor<Discount> discountArgumentCaptor = ArgumentCaptor.forClass(Discount.class);
        verify(discountRepository).save(discountArgumentCaptor.capture());
        Discount captureDiscount = discountArgumentCaptor.getValue();
        assertThat(captureDiscount).isEqualTo(discount);
    }

    @Test
    void updateDiscountById() {
        Discount oldDiscount = new Discount(1L, 2L, 3L, "title", "description", 20, new Date(12022020), new Date(12032020), 1, "image");
        discountService.createDiscount(oldDiscount);
        given(discountRepository.findById(oldDiscount.getId())).willReturn(Optional.of(oldDiscount));
        Discount newDiscount = new Discount();
        discountService.updateDiscountById(oldDiscount.getId(),  newDiscount);
        newDiscount.setTitle("title2");
        oldDiscount.setTitle(newDiscount.getTitle());
        discountService.createDiscount(oldDiscount);
        assertEquals("title2", oldDiscount.getTitle());

    }

    @Test
    void deleteDiscountById() {
        Discount discount = new Discount(1L, 2L, 3L, "title", "description", 20, new Date(12022020), new Date(12032020), 1, "image");
        discountService.createDiscount(discount);
        discountService.deleteDiscountById(1L);
        verify(discountRepository).deleteById(1L);
        Optional<Discount> discount1 = discountService.findByIdDiscount(1L);
        assertThat(discount1.isEmpty());


    }


}