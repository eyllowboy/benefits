package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.Discount;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;


import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class DiscountControllerTest {

    @Autowired
    private DiscountController discountController;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void test() throws Exception {
        assertThat(discountController).isNotNull();
    }



    @Test
    void allDiscount() throws Exception {
        this.mockMvc.perform(get("/discounts"))
                .andDo(print())
                .andExpect(status().isOk());


    }

    /*@Test
    void newDiscount() throws Exception {
        Discount oldDiscount1 = new Discount(5, 2, 6, "title1", "description", 20, new Date(12022020), new Date(12032020), 1, "image");
        this.mockMvc.perform(post("/discount/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isCreated());

    }*/







    @Test
    void oneDiscount() {
    }

    @Test
    void updateDiscount() {
    }

    @Test
    void deleteDiscount() {
    }

    @Test
    void filterDiscountByTitle() {
    }



    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }



}