package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Objects;
import java.util.Optional;

/**
 * A controller for handling requests for {@link LocationEntity}.
 *
 * @author Denis Popov
 * @version 1.0
 */

@ApiResponses(value = {
        @ApiResponse(responseCode = "401",
                description = "Unauthorized",
                content = @Content),
        @ApiResponse(responseCode = "403",
                description = "Forbidden",
                content = @Content),
        @ApiResponse(responseCode = "500",
                description = "Internal Server Error",
                content = @Content)
})
@Tag(name = "Location controller", description = "Controller for performing operations on locations.")
@RestController
@SecurityRequirement(name = "benefits")
public class LocationController {

    private final LocationService locationService;

    @Autowired
    public LocationController(final LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Create {@link LocationEntity} in the database.
     *
     * @param location new {@link LocationEntity} to be added
     * @throws IllegalStateException if {@link LocationEntity} with these parameters already exists.
     */
    @Operation(summary = "This is to create new location")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Location has been created",
                    content = @Content)
    })
    @PostMapping("/locations")
    public ResponseEntity<LocationEntity> addLocation(@RequestBody final LocationEntity location) {
        return new ResponseEntity<>(this.locationService.save(location), HttpStatus.CREATED);
    }

    /**
     * Gets {@link LocationEntity} from the database with specified id.
     *
     * @param id of {@link LocationEntity} that needs to get
     * @throws IllegalStateException if the given id was not found in the database
     * @return found {@link LocationEntity}
     */
    @Operation(summary = "This is to get the location by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Location has been received",
                    content = @Content)
    })
    @GetMapping("/locations/{id}")
    public LocationEntity getLocationById(@PathVariable final Long id) {
        return (this.locationService.findById(id));
    }

    /**
     * Get list of all {@link LocationEntity} from database.
     *
     * @param page is number of page to start returned result from
     * @param size is number of elements per page that needs to return
     * @param sort is the field by which to sort elements in returned list
     * @return a list of {@link LocationEntity} from database.
     */
    @Operation(summary = "This is to get all the stored locations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Details of all locations",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/locations")
    public Page<LocationEntity> getLocations(@RequestParam(required = false, defaultValue = "0") final int page,
                                             @RequestParam(required = false, defaultValue = "6") final int size,
                                             @RequestParam(required = false, defaultValue = "id") final String sort) {
        return this.locationService.findAll(PageRequest.of(page, size, Sort.by(sort)));
    }

    /**
     * Gets {@link LocationEntity} from the database with specified country and city name.
     *
     * @param country is the country of {@link LocationEntity} where to get city
     * @param city    is the name of {@link LocationEntity} that needs to get
     * @throws IllegalStateException if the given name was not found in the database
     * @return found {@link LocationEntity}
     */
    @Operation(summary = "This is to get the location by country and city name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Location has been received",
                    content = @Content)
    })

    @GetMapping("/locations/{country}/{city}")
    public LocationEntity getLocationByCountryAndCity(@PathVariable final String country,
                                                      @PathVariable final String city) {
        return (this.locationService.findByCity(country, city));    }

    /**
     * Get list of all {@link LocationEntity} in specified county from database.
     *
     * @param country  of {@link LocationEntity}
     * @param page is number of page to start returned result from
     * @param size is number of elements per page that needs to return
     * @param sort is the field by which to sort elements in returned list
     * @return a list of {@link LocationEntity} in specified county from database.
     */
    @Operation(summary = "This is to get all stored locations in specified country")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Details of all locations in specified country",
                    content = @Content(mediaType = "application/json"))
    })

    @GetMapping(value = "/locations/country")
    public Page<LocationEntity> findLocationByCountry(@RequestParam final String country,
                                                      @RequestParam(required = false, defaultValue = "0") final int page,
                                                      @RequestParam(required = false, defaultValue = "6") final int size,
                                                      @RequestParam(required = false, defaultValue = "id") final String sort) {
        return this.locationService.findByCountry(country, PageRequest.of(page, size, Sort.by(sort)));
    }

    /**
     * Get list of all {@link LocationEntity} from database which name starts from specified mask.
     *
     * @param country of the {@link LocationEntity} where to search
     * @param cityMask from which mast start city name in {@link LocationEntity}
     * @param page is number of page to start returned result from
     * @param size is number of elements per page that needs to return
     * @param sort is the field by which to sort elements in returned list
     * @return a list of {@link LocationEntity} in specified county from database.
     */
    @Operation(summary = "This is to get all stored locations which name starts with filter mask")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Details the locations in specified country starts with filter mask",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/locations/find-by-city")
    public Page<LocationEntity> findLocationByCityMask(@RequestParam final String country,
                                                       @RequestParam final String cityMask,
                                                       @RequestParam(required = false, defaultValue = "0") final int page,
                                                       @RequestParam(required = false, defaultValue = "6") final int size,
                                                       @RequestParam(required = false, defaultValue = "id") final String sort) {
        return this.locationService.findByCityMask(country, cityMask, PageRequest.of(page, size, Sort.by(sort)));
    }

    /**
     * Updates {@link LocationEntity} in the database.
     *
     * @param id of {@link LocationEntity} to be updated
     * @param updatedFields is {@link LocationEntity} in which fields with not-null values need to be updated
     * @throws IllegalStateException if the {@link LocationEntity} with given id was not found in the database.
     * @return updated {@link LocationEntity}
     */
    @Operation(summary = "This is to update the location")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Location has been updated",
                    content = @Content)
    })
    @PatchMapping("/locations/{id}")
    public ResponseEntity<LocationEntity> updateLocation(@PathVariable final Long id,
                                                         @RequestBody final LocationEntity updatedFields) {
        return ResponseEntity.ok(this.locationService.update(id, updatedFields));
    }

    /**
     * Deletes {@link LocationEntity} from the database.
     *
     * @param id of {@link LocationEntity} that needs to delete
     * @throws IllegalStateException if the given id was not found in the database
     */
    @Operation(summary = "This is to remove the location")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Location has been removed.",
                    content = @Content)
    })
    @DeleteMapping("/locations/{id}")
    public void deleteLocation(@PathVariable final Long id) {
        this.locationService.delete(id);
    }
}
