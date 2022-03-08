package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * A controller for handling requests for {@link LocationEntity}.
 *
 * @author Denis Popov
 * @version 1.0
 */
@Tag(name = "Location controller", description = "Controller for performing operations on locations.")
@RestController
public class LocationController {

    private final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Create {@link LocationEntity} in the database.
     *
     * @param country of the new location {@link LocationEntity#getCountry()}
     * @param city    of the new location {@link LocationEntity#getCity()}
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
    public final ResponseEntity<LocationEntity> addLocation(
            @RequestParam(value = "country") final String country,
            @RequestParam(value = "city") final String city) {
        locationService.findByCity(country, city).ifPresent(locationEntity -> {
            throw new IllegalStateException("Location with city name '" + city + "' and country '" + country + "' already exists");
        });
        final LocationEntity savedLocationEntity = locationService.save(new LocationEntity(country, city));
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
        return (locationService.findById(id)).orElseThrow(
                () -> new IllegalStateException("Location with this id was not found in the database"));
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
        return (locationService.findByCity(country, city)).orElseThrow(() ->
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
    @PutMapping("/locations")
    public void updateLocation(@RequestBody final LocationEntity location) {
        locationService.findById(location.getId())
                .orElseThrow(() -> new IllegalStateException("Location with this id was not found in the database"));
        locationService.updateLocationEntity(location.getId(), location.getCountry(), location.getCity());
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
        locationService.findById(id).orElseThrow(() ->
                new IllegalStateException("Location with id: '" + id + "' was not found in the database"));
        final Optional<LocationEntity> locationEntity = locationService.findWithAssociatedDiscounts(id);
        if (locationEntity.isPresent() && locationEntity.get().getDiscounts().size() > 0)
            throw new IllegalStateException("There is active discounts in this Location in database");
        locationService.delete(id);
    }

    /**
     * Get list of all {@link LocationEntity} from database.
     *
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
    public List<LocationEntity> getLocations() {
        return locationService.findAll();
    }

    /**
     * Get list of all {@link LocationEntity} in specified county from database.
     *
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
    public List<Optional<LocationEntity>> findByCountry(@RequestParam final String country) {
        return locationService.findByCountry(country);
    }

    /**
     * Get list of all {@link LocationEntity} from database which name starts from specified text.
     *
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
    public List<Optional<LocationEntity>> findByFirstLetters(@RequestParam final String country,
                                                             @RequestParam final String filterMask) {
        return locationService.findByFirstLetters(country, filterMask);
    }
}
