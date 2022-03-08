package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.domain.LocationEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    Optional<LocationEntity> findByCity(final String country, final String city);

    /***
     * Method to find all {@link LocationEntity} in specified country
     * @param country name of country to get all locations, not null
     * @return list of {@link LocationEntity} corresponding given name of country, error if nothing found
     */
    List<Optional<LocationEntity>> findByCountry(final String country);

    /***
     * Method to find all {@link LocationEntity} beginning with given letters in specified country
     * @param country name of country to get all locations, not null
     * @param filterMask case-insensitive beginning of {@link LocationEntity}'s name to search
     * @return list of {@link LocationEntity} corresponding given name of country
     */
    List<Optional<LocationEntity>> findByFirstLetters(final String country, final String filterMask);

    /***
     * Method to update location in database
     * @param id the id of {@link LocationEntity} in the database, not null
     * @param country the country of {@link LocationEntity} to store in the database, not null
     * @param city the name of city, not null
     */
    void updateLocationEntity(final Long id, final String country, final String city);

    /**
     * Method to get {@link LocationEntity} with EAGER fetch associated {@link DiscountEntity}
     * @param id the id of {@link LocationEntity} need to load, not null
     * @return {@link LocationEntity} with given id, error if id role found
     */
    Optional<LocationEntity> findWithAssociatedDiscounts(final Long id);
}
