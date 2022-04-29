package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.LocationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Interface for performing operations on a {@link LocationEntity} database.
 * @author Denis Popov
 * @version 1.0
 * @see CrudService
 * @see LocationEntity
 */
@Service
public interface LocationService extends CrudService<LocationEntity> {

    /***
     * Method to find {@link LocationEntity} by its name
     * @param country name of country where to search location, not null
     * @param city name of city to find, not null
     * @return {@link LocationEntity} corresponding given name from database, error if the name not found
     */
    LocationEntity findByCity(final String country, final String city);

    /***
     * Method to find all {@link LocationEntity} in specified country
     * @param country name of country to get all locations, not null
     * @return list of {@link LocationEntity} corresponding given name of country, error if nothing found
     */
    Page<LocationEntity> findByCountry(final String country, final Pageable pageable);

    /***
     * Method to find all {@link LocationEntity} beginning with given letters in specified country
     * @param country name of country to get all locations, not null
     * @param cityMask case-insensitive beginning of {@link LocationEntity}'s name to search
     * @return list of {@link LocationEntity} corresponding given name of country
     */
    Page<LocationEntity> findByCityMask(final String country, final String cityMask, final Pageable pageable);
}
