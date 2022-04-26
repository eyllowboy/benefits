package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.repository.LocationRepository;
import com.andersenlab.benefits.service.impl.LocationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "spring.main.lazy-initialization=true",
        classes = {LocationService.class, LocationServiceImpl.class})
public class LocationServiceTest {
    private final LocationService locationService;

    @MockBean
    private LocationRepository locationRepository;

    @Autowired
    public LocationServiceTest(final LocationService locationService) {
        this.locationService = locationService;
    }

    @Test
    public void whenFindAll() {
        // given
        final List<LocationEntity> locations = List.of(
                new LocationEntity("Россия", "Москва"),
                new LocationEntity("Украина", "Киев"),
                new LocationEntity("Белоруссия", "Минск"));
        final Page<LocationEntity> pageOfLocation = new PageImpl<>(locations);
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
        final LocationEntity locationEntity = new LocationEntity("Россия", "Москва");

        // when
        when(this.locationRepository.findById(1L)).thenReturn(Optional.of(locationEntity));
        final LocationEntity foundLocation = this.locationService.findById(1L);

        // then
        assertEquals(locationEntity, foundLocation);
        verify(this.locationRepository, times(1)).findById(1L);
    }

    @Test
    public void whenFindByCity() {
        // given
        final LocationEntity locationEntity = new LocationEntity("Россия", "Казань");

        // when
        when(this.locationRepository.findByCity("Россия", "Казань")).thenReturn(Optional.of(locationEntity));
        final Optional<LocationEntity> foundLocation = this.locationService.findByCity("Россия", "Казань");

        // then
        assertEquals(Optional.of(locationEntity), foundLocation);
        verify(this.locationRepository, times(1)).findByCity("Россия", "Казань");
    }

    @Test
    public void whenAddLocation() {
        // given
        final LocationEntity locationEntity = new LocationEntity("Россия", "Казань");

        // when
        when(this.locationRepository.save(any(LocationEntity.class))).thenReturn(locationEntity);
        final LocationEntity savedLocation = this.locationService.save(locationEntity);

        // then
        assertEquals(locationEntity, savedLocation);
        verify(this.locationRepository, times(1)).save(locationEntity);
    }

    @Test
    public void whenUpdateLocation() {
        // when
        this.locationRepository.updateLocationEntity(anyLong(), anyString(), anyString());

        // then
        verify(this.locationRepository, times(1)).updateLocationEntity(anyLong(), anyString(), anyString());
    }

    @Test
    public void whenDeleteLocation() {
        // when
        this.locationService.delete(anyLong());

        // then
        verify(this.locationRepository, times(1)).deleteById(anyLong());
    }

}
