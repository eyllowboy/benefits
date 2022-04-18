package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.repository.LocationRepository;
import com.andersenlab.benefits.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
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
        return this.locationRepository.findByCity(country, city);
    }

    @Override
    public List<LocationEntity> findAll() {
        return this.locationRepository.findAll();
    }

    @Override
    public List<Optional<LocationEntity>> findByCountry(final String country) {
        return this.locationRepository.findByCountry(country);
    }

    @Override
    public List<Optional<LocationEntity>> findByFirstLetters(final String country, final String filterMask) {
        return this.locationRepository.findByFirstLetters(country, filterMask);
    }

    @Override
    @Transactional
    public void updateLocationEntity(final Long id, final String country, final String city) {
        final LocationEntity location = new LocationEntity(id, country, city);
        ValidateUtils.validateEntityPatch(location);
        this.locationRepository.updateLocationEntity(location.getId(), location.getCountry(), location.getCity());
    }

    @Override
    public Optional<LocationEntity> findById(final Long id) {
        return this.locationRepository.findById(id);
    }

    @Override
    public LocationEntity save(final LocationEntity entity) {
        ValidateUtils.validateEntityPost(entity);
        return this.locationRepository.save(entity);
    }

    @Override
    public void delete(final Long id) {
        this.locationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Optional<LocationEntity> findWithAssociatedDiscounts(final Long id) {
        return this.locationRepository.findWithAssociatedDiscounts(id);
    }
}
