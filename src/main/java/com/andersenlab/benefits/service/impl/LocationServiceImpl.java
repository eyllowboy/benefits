package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.repository.LocationRepository;
import com.andersenlab.benefits.service.LocationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Objects;
import java.util.Optional;

import static com.andersenlab.benefits.service.impl.ValidateUtils.*;

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
    public LocationEntity findById(final Long id) {
        return this.locationRepository.findById(id).orElseThrow(() ->
                new IllegalStateException(errIdNotFoundMessage("Location", id)));
    }

    @Override
    public LocationEntity findByCity(final String country, final String city) {
        return this.locationRepository.findByCountryAndCity(country, city).orElseThrow(() -> {
            throw new IllegalStateException(
                    errEntityNotFoundMessage("Location", "city name", city));}
        );
    }

    @Override
    public Page<LocationEntity> findAll(final Pageable pageable) {
        return this.locationRepository.findAll(pageable);
    }

    @Override
    public Page<LocationEntity> findByCountry(final String country, final Pageable pageable) {
        return this.locationRepository.findByCountry(country, pageable);
    }

    @Override
    public Page<LocationEntity> findByCityMask(final String country, final String cityMask, final Pageable pageable) {
        return this.locationRepository.findByCountryAndCityStartsWith(country, cityMask, pageable);
    }

    @Override
    @Transactional
    public LocationEntity update(final Long id, final LocationEntity location) {
        if (!Objects.isNull(location.getCity())) {
            final Optional<LocationEntity> theSameLocation = this.locationRepository.findByCity(location.getCity());
            if (theSameLocation.isPresent() && !theSameLocation.get().getId().equals(id))
                throw new IllegalStateException(
                        errAlreadyExistMessage("Location", "city name", location.getCity()));
        }
        final LocationEntity existingLocation = findById(id);
        BeanUtils.copyProperties(location, existingLocation, "id");
        validateEntityFieldsAnnotations(location, false);
        return this.locationRepository.save(existingLocation);
    }

    @Override
    public LocationEntity save(final LocationEntity location) {
        this.locationRepository.findByCity(location.getCity()).ifPresent(foundLocation -> {
            throw new IllegalStateException(
                    errAlreadyExistMessage("Location", "city name", location.getCity()));}
        );
        location.setId(null);
        validateEntityFieldsAnnotations(location, true);
        return this.locationRepository.save(location);
    }

    @Override
    public void delete(final Long id) {
        final LocationEntity existingLocation = findById(id);
        final Optional<LocationEntity> location = this.locationRepository.findWithAssociatedDiscounts(id);
        if (location.isPresent() && location.get().getDiscounts().size() > 0) {
            throw new IllegalStateException(
                    errAssociatedEntity("discounts", "Location"));
        }
        this.locationRepository.delete(existingLocation);
    }
}
