package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.repository.LocationRepository;
import com.andersenlab.benefits.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
    public Page<LocationEntity> findAll(final Pageable pageable) {
        return locationRepository.findAll(pageable);
    }

    @Override
    public Page<LocationEntity> findByCountry(final String country, final Pageable pageable) {
        return locationRepository.findByCountry(country,pageable);
    }

    @Override
    public Page<LocationEntity> findByFirstLetters(final String country, final String filterMask, final Pageable pageable) {
        return locationRepository.findByFirstLetters(country, filterMask, pageable);
    }

    @Override
    @Transactional
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

    @Override
    @Transactional
    public Optional<LocationEntity> findWithAssociatedDiscounts(final Long id) {
        return locationRepository.findWithAssociatedDiscounts(id);
    }
}
