package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.util.NestedServletException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@WithMockUser
public class LocationControllerTest {

    private final MockMvc mockMvc;
    private final LocationRepository locationRepository;
    private final DiscountRepository discountRepository;
    private final ControllerTestUtils ctu;

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres")
                    .withDatabaseName("benefits")
                    .withUsername("benefits")
                    .withPassword("ben0147");

    @Autowired
    public LocationControllerTest(final MockMvc mockMvc,
                                  final LocationRepository locationRepository,
                                  final DiscountRepository discountRepository,
                                  final ControllerTestUtils ctu) {
        this.mockMvc = mockMvc;
        this.locationRepository = locationRepository;
        this.discountRepository = discountRepository;
        this.ctu = ctu;
    }

    @DynamicPropertySource
    public static void postgreSQLProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    }

    @BeforeEach
    private void deleteAndSaveLocationInContainer() {
        this.ctu.clearTables();
        createAndSaveLocationInContainer();
    }

    private void createAndSaveLocationInContainer() {
        final int size = 5;
        for (long i = 1; i <= size; i++) {
            LocationEntity location = new LocationEntity("Country" + i, "City" + i);
            this.locationRepository.save(location);
        }
    }

    @Test
    public void whenGetAllLocationsSuccess() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    public void whenGetAllLocationsInCountrySuccess() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("country", "Country10")
                        .with(csrf()))
                .andDo(print())
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    public void whenGetLocationByIdSuccess() throws Exception {
        final LocationEntity lastLocationFromContainer = this.locationRepository.findByCity("Country5", "City5").get();
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/locations/{id}", lastLocationFromContainer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(lastLocationFromContainer.getId().intValue())))
                .andExpect(jsonPath("$.country", is(lastLocationFromContainer.getCountry())))
                .andExpect(jsonPath("$.city", is(lastLocationFromContainer.getCity())));
    }

    @Test
    public void whenGetLocationByIdFailIdNotExists() throws Exception {
        // given
        final LocationEntity lastLocationFromContainer = this.locationRepository.findByCity("Country5", "City5").get();
        long notExistId = lastLocationFromContainer.getId()+1;
        // when
        final NestedServletException NestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(get("/locations/{id}", notExistId).with(csrf())));
        // then
        assertEquals(IllegalStateException.class, NestedServletException.getCause().getClass());
        assertEquals("Location with this id was not found in the database",
                NestedServletException.getCause().getMessage());
    }

    @Test
    public void whenGetLocationByCitySuccess() throws Exception {
        // given
        final LocationEntity lastLocationFromContainer = this.locationRepository.findByCity("Country5", "City5").get();
        String country = lastLocationFromContainer.getCountry();
        String city = lastLocationFromContainer.getCity();
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/locations/{country}/{city}", "Country5", "City5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.country", is(country)))
                .andExpect(jsonPath("$.city", is(city)));
    }

    @Test
    public void whenGetLocationByCityFailNotExists() throws Exception {
        // given
        final String country = "Россия";
        final String city = "Тьмутаракань";
        // when
        final NestedServletException NestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                                .get("/locations/{country}/{city}", country, city)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                        .andDo(print()));
        // then
        assertEquals(IllegalStateException.class, NestedServletException.getCause().getClass());
        assertEquals("Location with city name '" + city + "' and country '" + country + "' was not found in the database",
                NestedServletException.getCause().getMessage());
    }

    @Test
    public void whenAddLocationSuccess() throws Exception {
        // given
        final LocationEntity location = new LocationEntity("Россия", "Пермь");

        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(location))
                        .with(csrf()))
                .andDo(print())
                // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.country", is(location.getCountry())))
                .andExpect(jsonPath("$.city", is(location.getCity())));
    }

    @Test
    public void whenAddLocationFailLocationExists() throws Exception {
        // given
        final LocationEntity location = this.locationRepository.findByCity("Country5", "City5").get();

        // when
        final NestedServletException NestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(location))
                        .with(csrf())));

        // then
        assertEquals(IllegalStateException.class, NestedServletException.getCause().getClass());
        assertEquals("Location with city name '" + location.getCity() + "' and country '" + location.getCountry() + "' already exists",
                NestedServletException.getCause().getMessage());
    }

    @Test
    public void whenUpdateLocationSuccess() throws Exception {
        // given
        final LocationEntity lastLocationFromContainer = this.locationRepository.findByCity("Country5", "City5").get();
        lastLocationFromContainer.setCountry("Россия");
        final String locationEntity = new ObjectMapper().writeValueAsString(lastLocationFromContainer);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .put("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(locationEntity)
                        .with(csrf()))
                .andDo(print())
                // then
                .andExpect(status().isOk());
    }

    @Test
    public void whenUpdateLocationFailIdNotExists() throws Exception {
        // given
        final LocationEntity lastLocationFromContainer = this.locationRepository.findByCity("Country5", "City5").get();
        lastLocationFromContainer.setId(lastLocationFromContainer.getId()+1);
        final String locationEntity = new ObjectMapper().writeValueAsString(lastLocationFromContainer);
        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                        .put("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(locationEntity)
                        .with(csrf())));
        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("Location with this id was not found in the database",
                nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenDeleteLocationWithoutDiscountsSuccess() throws Exception {
        // given
        final LocationEntity lastLocationFromContainer = this.locationRepository.findByCity("Country5", "City5").get();
        final Long id = lastLocationFromContainer.getId();
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/locations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                // then
                .andExpect(status().isOk());
    }

    @Test
    public void whenDeleteLocationFailHasActiveDiscounts() throws Exception {
        // given
        final LocationEntity location = this.locationRepository.save(this.ctu.getLocation(this.ctu.getRndEntityPos()));
        final DiscountEntity discount = this.ctu.getDiscount(this.ctu.getRndEntityPos());
        discount.setArea(Set.of(location));
        this.discountRepository.save(discount);
        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/locations/{id}", location.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())));
        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("There is active discounts in this Location in database",
                nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenDeleteLocationFailIdNotExists() throws Exception {
        // given
        final LocationEntity lastLocationFromContainer = this.locationRepository.findByCity("Country5", "City5").get();
        final Long id = lastLocationFromContainer.getId()+1;
        // when
        final NestedServletException nestedServletException = assertThrows(NestedServletException.class, () ->
                this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/locations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())));
        // then
        assertEquals(IllegalStateException.class, nestedServletException.getCause().getClass());
        assertEquals("Location with id: '" + id + "' was not found in the database",
                nestedServletException.getCause().getMessage());
    }

    @Test
    public void whenGetLocationByMaskSuccess() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("country", "Contry5")
                        .param("filterMask", "са")
                        .with(csrf()))
                .andDo(print())
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "    "})
    public void whenAddLocationWrongObligatoryFields(final String city) throws Exception {
        final LocationEntity location = this.ctu.getLocation(this.ctu.getRndEntityPos());
        location.setCity(city);
        final MvcResult result;

        // when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(location))
                        .with(csrf()))
                .andReturn();

        // then
        assertEquals(400, result.getResponse().getStatus());
        final String errorResult = Objects.requireNonNull(result.getResolvedException()).getMessage();
        assertTrue(errorResult.contains("must not be blank"));
    }

    @ParameterizedTest
    @ValueSource(strings = {" space at start", "space at end ", " two  spaces  inside", " three   spaces   inside"})
    public void whenAddLocationTrimFields(final String city) throws Exception {
        final LocationEntity location = this.ctu.getLocation(this.ctu.getRndEntityPos());
        location.setCity(city);
        final LocationEntity postedLocation;
        String postedCity;
        final MvcResult result;

        //when
        result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(location))
                        .with(csrf()))
                .andReturn();

        // then
        assertEquals(201, result.getResponse().getStatus());
        postedLocation = this.ctu.getLocationFromJson(new JSONObject(result.getResponse().getContentAsString()));
        postedCity = city.trim();
        while (postedCity.contains("  "))
            postedCity = postedCity.replace("  ", " ");
        assertEquals(postedCity, postedLocation.getCity());
    }
}