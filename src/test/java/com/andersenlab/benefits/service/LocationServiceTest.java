package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.CompanyEntity;
import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.repository.LocationRepository;
import com.andersenlab.benefits.service.impl.LocationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static com.andersenlab.benefits.service.ServiceTestUtils.*;
import static com.andersenlab.benefits.service.impl.ValidateUtils.errIdNotFoundMessage;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "spring.main.lazy-initialization=true",
        classes = {LocationService.class, LocationServiceImpl.class})
public class LocationServiceTest {
    private final LocationService locationService;
    private final List<LocationEntity> locations = new ArrayList<>();

    @MockBean
    private LocationRepository locationRepository;

    @Autowired
    public LocationServiceTest(final LocationService locationService) {
        this.locationService = locationService;
    }

    @BeforeEach
    public void ResetData() {
        when(this.locationRepository.findAll(any(Pageable.class))).thenAnswer(invocation ->
                new PageImpl<>(this.locations, invocation.getArgument(0), 100));
        when(this.locationRepository.findAll()).thenReturn(this.locations);
        when(this.locationRepository.findById(anyLong())).thenAnswer(invocation ->
                this.locations.stream().filter(location ->
                        Objects.equals(location.getId(), invocation.getArgument(0))).findFirst());
        when(this.locationRepository.findByCountryAndCity(anyString(), anyString())).thenAnswer(invocation ->
                this.locations.stream().filter(location ->
                        location.getCountry().equals(invocation.getArgument(0))
                        && location.getCity().equals(invocation.getArgument(1))).findFirst());
        when(this.locationRepository.save(any(LocationEntity.class))).thenAnswer(invocation -> {
            final LocationEntity savedLocation = invocation.getArgument(0);
            savedLocation.setId((long) this.locations.size());
            this.locations.add(savedLocation);
            return invocation.getArgument(0);
        });
        when(this.locationRepository.saveAll(anyList())).thenAnswer(invocation -> {
            final List<LocationEntity> locationsToSave = invocation.getArgument(0);
            locationsToSave.forEach(item -> saveItem(this.locations, item, Objects::equals));
            return locationsToSave;
        });
        doAnswer(invocation -> this.locations.remove((LocationEntity) invocation.getArgument(0)))
                .when(this.locationRepository).delete(any(LocationEntity.class));
        this.locations.clear();
        this.locationRepository.saveAll(getLocationList(10));
    }

    @Test
    public void whenFindAll() {
        // given
        final Page<LocationEntity> pageOfLocation = new PageImpl<>(this.locations);

        // when
        when(this.locationRepository.findAll(PageRequest.of(0,3))).thenReturn(pageOfLocation);
        final Page<LocationEntity> foundLocations = this.locationService.findAll(PageRequest.of(0,3));

        // then
        assertEquals(pageOfLocation, foundLocations);
        verify(this.locationRepository, times(1)).findAll(PageRequest.of(0,3));
    }

    @Test
    public void whenFindById() {
        // given
        final LocationEntity location = this.locations.get(getRndEntityPos() - 1);

        // when
        final LocationEntity foundLocation = this.locationService.findById(location.getId());

        // then
        assertEquals(location, foundLocation);
        verify(this.locationRepository, times(1)).findById(location.getId());
    }

    @Test
    public void whenFindByCity() {
        // given
        final LocationEntity location = this.locations.get(getRndEntityPos() - 1);

        // when
        final LocationEntity foundLocation = this.locationService.findByCity(location.getCountry(), location.getCity());

        // then
        assertEquals(location, foundLocation);
        verify(this.locationRepository, times(1)).findByCountryAndCity(location.getCountry(), location.getCity());
    }

    @Test
    public void whenAddLocation() {
        // given
        final int sizeBeforeSave = this.locations.size();
        final LocationEntity location = getLocation(getRndEntityPos() - 1);

        // when
        final LocationEntity savedLocation = this.locationService.save(location);

        // then
        assertEquals(location, savedLocation);
        assertEquals(sizeBeforeSave + 1, this.locationRepository.findAll().size());
        verify(this.locationRepository, times(1)).save(location);
    }

    @Test
    void whenUpdateLocation() {
        //given
        final LocationEntity location = this.locations.get(getRndEntityPos() - 1);
        location.setCity("New City");

        //when
        final LocationEntity updatedLocation = this.locationService.update(location.getId(), location);

        //then
        assertThat(updatedLocation.getCity()).isEqualTo("New City");
    }


    @Test
    void whenDeleteLocationSuccess() {
        final int sizeBeforeDelete = this.locations.size();
        final LocationEntity location = this.locations.get(getRndEntityPos() - 1);

        //when
        this.locationService.delete(location.getId());

        //then
        assertEquals(sizeBeforeDelete - 1, this.locationRepository.findAll().size());
        assertEquals(Optional.empty(), this.locationRepository.findById(location.getId()));
        verify(this.locationRepository, times(1)).delete(location);
    }

    @Test
    public void whenDeleteLocationFail() {
        // given
        final LocationEntity location = getLocation(getRndEntityPos());

        // when
        final Exception ex = assertThrows(IllegalStateException.class, () ->
                this.locationService.delete(location.getId()));

        assertEquals(errIdNotFoundMessage("Location", location.getId()), ex.getMessage());
        verify(this.locationRepository, times(0)).delete(location);
    }

}
