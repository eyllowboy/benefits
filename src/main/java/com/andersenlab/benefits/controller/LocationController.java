package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.CategoryEntity;
import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Objects;
import java.util.Optional;

/**
 * A controller for handling requests for {@link LocationEntity}.
 *
 * @author Denis Popov
 * @version 1.0
 */
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
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @PostMapping("/locations")
    public ResponseEntity<LocationEntity> addLocation(@Valid @RequestBody final LocationEntity location) {
        this.locationService.findByCity(location.getCountry(), location.getCity()).ifPresent(locationEntity -> {
            throw new IllegalStateException("Location with city name '" + location.getCity() + "' and country '" + location.getCountry() + "' already exists");
        });
        final LocationEntity savedLocationEntity = this.locationService.save(location);
        return new ResponseEntity<>(savedLocationEntity, HttpStatus.CREATED);
    }

    /**
     * Gets {@link LocationEntity} from the database with specified id.
     *
     * @param id is the id of {@link LocationEntity} that needs to get
     * @throws IllegalStateException if the given id was not found in the database
     */
    @Operation(summary = "This is to get the location by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Location has been received",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content
            )
    })
    @GetMapping("/locations/{id}")
    public LocationEntity getLocationById(@PathVariable final Long id) {
        return (this.locationService.findById(id));
//                .orElseThrow(
//                () -> new IllegalStateException("Location with this id was not found in the database"));
    }

    /**
     * Gets {@link LocationEntity} from the database with specified country and city name.
     *
     * @param country is the country {@link LocationEntity} where to get city
     * @param city    is the name of {@link LocationEntity} that needs to get
     * @throws IllegalStateException if the given name was not found in the database
     */
    @Operation(summary = "This is to get the location by country and city name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Location has been received",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content
            )
    })
    @GetMapping("/locations/{country}/{city}")
    public LocationEntity getLocationByName(@PathVariable final String country, @PathVariable final String city) {
        return (this.locationService.findByCity(country, city)).orElseThrow(() ->
                new IllegalStateException("Location with city name '" + city + "' and country '" + country + "' was not found in the database"));
    }

    /**
     * Updates {@link LocationEntity} in the database.
     *
     * @param location the {@link LocationEntity} that needs to update
     * @throws IllegalStateException if the {@link LocationEntity} with given id was not found in the database.
     */
    @Operation(summary = "This is to update the location")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Location has been updated",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @PatchMapping("/locations/{id}")
    public ResponseEntity<LocationEntity> updateLocation(@PathVariable final Long id,
                                                         @RequestBody final LocationEntity location) {
        if (!Objects.isNull(location.getCity())) {
            final Optional<LocationEntity> theSameLocation = this.locationService.findByCity(location.getCountry(), location.getCity());
            if (theSameLocation.isPresent() && !theSameLocation.get().getId().equals(id))
                throw new IllegalStateException("Location with city '" + location.getCity() + "' already exists");
        }
        final LocationEntity existingLocation = this.locationService.findById(id);
//                .orElseThrow(() -> new IllegalStateException("Location with this id was not found in the database"));
        BeanUtils.copyProperties(location, existingLocation, "id");
        this.locationService.updateLocationEntity(id, existingLocation.getCountry(), existingLocation.getCity());
        return ResponseEntity.ok(existingLocation);
    }

    /**
     * Deletes {@link LocationEntity} from the database.
     *
     * @param id is the id of {@link LocationEntity} that needs to delete
     * @throws IllegalStateException if the given id was not found in the database
     */
    @Operation(summary = "This is to remove the location")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Location has been removed.",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @DeleteMapping("/locations/{id}")
    public void deleteLocation(@PathVariable final Long id) {
        this.locationService.findById(id);
//                .orElseThrow(() ->
//                new IllegalStateException("Location with id: '" + id + "' was not found in the database"));
        final Optional<LocationEntity> locationEntity = this.locationService.findWithAssociatedDiscounts(id);
        if (locationEntity.isPresent() && locationEntity.get().getDiscounts().size() > 0)
            throw new IllegalStateException("There is active discounts in this Location in database");
        this.locationService.delete(id);
    }

    /**
     * Get list of all {@link LocationEntity} from database.
     *
     * @param page is the page of {@link CategoryEntity} that needs to pagination
     * @param size is the count of {@link CategoryEntity} that needs to pagination
     * @param sort is the sort of {@link CategoryEntity} that needs to pagination
     * @return a list of {@link LocationEntity} from database.
     */
    @Operation(summary = "This is to fetch all the stored locations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Details of all locations",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @GetMapping(value = "/locations")
    public Page<LocationEntity> getLocations(@RequestParam(required = false, defaultValue = "0") final int page,
                                             @RequestParam(required = false, defaultValue = "6") final int size,
                                             @RequestParam(required = false, defaultValue = "id") final String sort) {
        return this.locationService.findAll(PageRequest.of(page, size, Sort.by(sort)));
    }

    /**
     * Get list of all {@link LocationEntity} in specified county from database.
     *
     * @param country  of {@link CategoryEntity}
     * @param page is the page of {@link CategoryEntity} that needs to pagination
     * @param size is the count of {@link CategoryEntity} that needs to pagination
     * @param sort is the sort of {@link CategoryEntity} that needs to pagination
     * @return a list of {@link LocationEntity} in specified county from database.
     */
    @Operation(summary = "This is to fetch all stored locations in specified country")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Details of all locations in specified country",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "/locations/country")
    public Page<LocationEntity> findByCountry(@RequestParam final String country,
                                              @RequestParam(required = false, defaultValue = "0") final int page,
                                              @RequestParam(required = false, defaultValue = "6") final int size,
                                              @RequestParam(required = false, defaultValue = "id") final String sort) {
        return this.locationService.findByCountry(country, PageRequest.of(page, size, Sort.by(sort)));
    }

    /**
     * Get list of all {@link LocationEntity} from database which name starts from specified text.
     *
     * @param page is the page of {@link CategoryEntity} that needs to pagination
     * @param size is the count of {@link CategoryEntity} that needs to pagination
     * @param sort is the sort of {@link CategoryEntity} that needs to pagination
     * @return a list of {@link LocationEntity} in specified county from database.
     */
    @Operation(summary = "This is to fetch all stored locations which name starts with filter mask")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Details the locations in specified country starts with filter mask",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "/locations/filter", params = {"country", "filterMask"})
    public Page<LocationEntity> findByFirstLetters(@RequestParam final String country,
                                                   @RequestParam final String filterMask,
                                                   @RequestParam(required = false, defaultValue = "0") final int page,
                                                   @RequestParam(required = false, defaultValue = "6") final int size,
                                                   @RequestParam(required = false, defaultValue = "id") final String sort) {
        return this.locationService.findByFirstLetters(country, filterMask, PageRequest.of(page, size, Sort.by(sort)));
    }
}
