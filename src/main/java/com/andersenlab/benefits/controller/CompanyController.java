package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.CompanyEntity;
import com.andersenlab.benefits.service.CompanyService;
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

/**
 * A controller for handling requests for {@link CompanyEntity}.
 *
 * @author Aleksei Sidorin
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
@Tag(name = "Company controller", description = "Controller for the performing operation on companies.")
@RestController
@SecurityRequirement(name = "benefits")
public class CompanyController {

    private final CompanyService companyService;

    @Autowired
    public CompanyController(final CompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * Create {@link CompanyEntity} in the database.
     *
     * @param companyEntity new {@link CompanyEntity} to be added
     * @throws IllegalStateException if {@link CompanyEntity} with the same Title already exists
     * @return created {@link CompanyEntity}
     */
    @Operation(summary = "This method created the new company.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Company has been created",
                    content = @Content)
    })
    @PostMapping("/companies")
    public ResponseEntity<CompanyEntity> addCompany(@RequestBody final CompanyEntity companyEntity) {
        return new ResponseEntity<>(this.companyService.save(companyEntity), HttpStatus.CREATED);
    }

    /**
     * Gets {@link CompanyEntity} from the database with specified id.
     *
     * @param id is the id of {@link CompanyEntity} that needs to get.
     * @throws IllegalStateException if the given id was not found in the database.
     * @return found {@link CompanyEntity}
     */
    @Operation(summary = "This is to get the company by the id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Company has been received",
                    content = @Content)
    })
    @GetMapping("/companies/{id}")
    public CompanyEntity getCompanyById(@PathVariable final Long id) {
        return (this.companyService.findById(id));
    }

    /**
     * Gets all {@link CompanyEntity} from the database
     *
     * @param page is number of page to start returned result from
     * @param size is number of elements per page that needs to return
     * @param sort is the field by which to sort elements in returned list
     * @return a list of {@link CompanyEntity} from database.
     */
    @Operation(summary = "This is to get all companies from the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Details of all companies.",
                    content = @Content)
    })
    @GetMapping("/companies")
    public Page<CompanyEntity> getAllCompany(@RequestParam(required = false, defaultValue = "0") final int page,
                                             @RequestParam(required = false, defaultValue = "6") final int size,
                                             @RequestParam(required = false, defaultValue = "id") final String sort) {
        return this.companyService.findAll(PageRequest.of(page, size, Sort.by(sort)));
    }


    /**
     * Updates {@link CompanyEntity} in the database.
     *
     * @param id of {@link CompanyEntity} to be updated
     * @param company is {@link CompanyEntity} in which fields with not-null values need to be updated
     * @throws IllegalStateException if the {@link CompanyEntity} with given id was not found in the database.
     * @return updated {@link CompanyEntity}
     */
    @Operation(summary = "This is to update the company.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Company has been updated.",
                    content = @Content)
    })
    @PatchMapping("/companies/{id}")
    public ResponseEntity<CompanyEntity> updatedCompany(@PathVariable final Long id,
                                                        @RequestBody final CompanyEntity company) {
        return ResponseEntity.ok(this.companyService.update(id, company));
    }

    /**
     * Deletes {@link CompanyEntity} from the database.
     *
     * @param id the id of {@link CompanyEntity} that needs to delete.
     * @throws IllegalStateException if the given id was not found in the database.
     */
    @Operation(summary = "This is to remove the company.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Company has been deleted",
                    content = @Content)
    })
    @DeleteMapping("/companies/{id}")
    public void deleteCompanyById(@PathVariable final Long id) {
        this.companyService.delete(id);
    }
}
