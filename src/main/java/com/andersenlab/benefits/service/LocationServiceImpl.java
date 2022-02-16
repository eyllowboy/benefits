package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/***
 * Implementation for performing operations on a {@link LocationEntity}
 * @author Denis Popov
 * @version 1.0
 * @see LocationService
 * @see LocationEntity
 */
@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Autowired
    public LocationServiceImpl(final LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public Optional<LocationEntity> findByCity(final String country, final String city) {
        return locationRepository.findByCity(country, city);
    }

    @Override
    public List<LocationEntity> findAll() {
        return locationRepository.findAll();
    }

    @Override
    public List<Optional<LocationEntity>> findByCountry(final String country) {
        return locationRepository.findByCountry(country);
    }

    @Override
    public List<Optional<LocationEntity>> findByFirstLetters(final String country, final String filterMask) {
        return locationRepository.findByFirstLetters(country, filterMask);
    }

    @Override
    public void updateLocationEntity(final Long id, final String country, final String city) {
        locationRepository.updateLocationEntity(id, country, city);
    }

    @Override
    public Optional<LocationEntity> findById(final Long id) {
        return locationRepository.findById(id);
    }

    @Override
    public LocationEntity save(final LocationEntity entity) {
        return locationRepository.save(entity);
    }

    @Override
    public void delete(final Long id) {
        locationRepository.deleteById(id);
    }
}
