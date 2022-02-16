package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.LocationEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.util.NestedServletException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class LocationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres")
                    .withDatabaseName("benefits")
                    .withUsername("benefits")
                    .withPassword("ben0147");

    @DynamicPropertySource
    public static void postgreSQLProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    }

    @Test
    public void whenGetAllLocationsSuccess() throws Exception {
        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/locations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
        // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    public void whenGetAllLocationsInCountrySuccess() throws Exception {
        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("country", "Россия"))
                .andDo(print())
        // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    public void whenGetLocationByIdSuccess() throws Exception {
        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
        // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.country", is("Россия")))
                .andExpect(jsonPath("$.city", is("Москва")));
    }

    @Test
    public void whenGetLocationByIdFailIdNotExists() throws Exception {
        // when
        final NestedServletException NestedServletException = assertThrows(NestedServletException.class, () ->
                mockMvc.perform(get("/locations/{id}", 100L)));
        // then
        assertEquals(IllegalStateException.class, NestedServletException.getCause().getClass());
        assertEquals("Location with this id was not found in the database",
                NestedServletException.getCause().getMessage());
    }

    @Test
    public void whenGetLocationByCitySuccess() throws Exception {
        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/locations/{country}/{city}", "Россия", "Казань")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.country", is("Россия")))
                .andExpect(jsonPath("$.city", is("Казань")));
    }

    @Test
    public void whenGetLocationByCityFailNotExists() throws Exception {
        // given
        final String country = "Россия";
        final String city = "Тьмутаракань";
        // when
        final NestedServletException NestedServletException = assertThrows(NestedServletException.class, () ->
                mockMvc.perform(MockMvcRequestBuilders
                        .get("/locations/{country}/{city}", country, city)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()));
                // then
        assertEquals(IllegalStateException.class, NestedServletException.getCause().getClass());
        assertEquals("Location with city name '" + city + "' and country '" + country + "' was not found in the database",
                NestedServletException.getCause().getMessage());
    }

    @Test
    public void whenAddLocationSuccess() throws Exception {
        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("country", "Россия")
                        .param("city", "Пермь"))
                .andDo(print())
        // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.country", is("Россия")))
                .andExpect(jsonPath("$.city", is("Пермь")));
    }

    @Test
    public void whenAddLocationFailLocationExists() throws Exception {
        // given
        final String country = "Россия";
        final String city = "Самара";
        // when
        final NestedServletException NestedServletException = assertThrows(NestedServletException.class, () ->
                mockMvc.perform(MockMvcRequestBuilders
                        .post("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("country", country)
                        .param("city", city)));
        // then
        assertEquals(IllegalStateException.class, NestedServletException.getCause().getClass());
        assertEquals("Location with city name '" + city + "' and country '" + country + "' already exists",
                NestedServletException.getCause().getMessage());
    }

    @Test
    public void whenUpdateLocationSuccess() throws Exception {
        // given
        final LocationEntity location = new LocationEntity(2L,"Россия", "СПБ");
        final String locationEntity = new ObjectMapper().writeValueAsString(location);
        // when
        mockMvc.perform(MockMvcRequestBuilders
                .put("/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(locationEntity))
                .andDo(print())
        // then
                .andExpect(status().isOk());
    }

    @Test
    public void whenUpdateLocationFailIdNotExists() throws Exception {
        // given
        LocationEntity location = new LocationEntity(100L,"Россия", "Moscow");
        final String locationEntity = new ObjectMapper().writeValueAsString(location);
        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                mockMvc.perform(MockMvcRequestBuilders
                        .put("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(locationEntity)));
        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("Location with this id was not found in the database",
                nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenDeleteLocationSuccess() throws Exception {
        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
        // then
                .andExpect(status().isOk());
    }

    @Test
    public void whenDeleteLocationFailIdNotExists() throws Exception {
        // given
        final Long id = 100L;
        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
            mockMvc.perform(MockMvcRequestBuilders
                        .delete("/locations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)));
        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("Location with id: '"+ id +"' was not found in the database",
                nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenGetLocationByMaskSuccess() throws Exception {
        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("country", "Россия")
                        .param("filterMask", "са"))
                .andDo(print())
        // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }
}
