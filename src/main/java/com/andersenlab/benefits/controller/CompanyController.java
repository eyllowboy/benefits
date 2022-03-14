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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * A controller for handling requests for {@link CompanyEntity}.
 *
 * @author Aleksei Sidorin
 * @version 1.0
 */

@Tag(name = "Company controller", description = "Controller for the performing operation on companies.")
@RestController
@SecurityRequirement(name = "benefits")
public class CompanyController {

    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * Create {@link CompanyEntity} in the database.
     *
     * @param companyEntity new company {@link CompanyEntity}
     * @throws IllegalStateException if {@link CompanyEntity} already had created.
     */
    @Operation(summary = "This method created the new company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Location has been created",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @PostMapping("/companies")
    public final Optional<CompanyEntity> addCompany(@RequestBody CompanyEntity companyEntity) {
        return Optional.ofNullable(companyService.createCompany(companyEntity)
                .orElseThrow(() -> new IllegalStateException("The company with id: " + companyEntity.getId() + " already exists.")));
    }

    /**
     * Gets {@link CompanyEntity} from the database with specified id.
     *
     * @param id is the id of {@link CompanyEntity} that needs to get.
     * @throws IllegalStateException if the given id was not found in the database.
     */
    @Operation(summary = "This method gets the company by the id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Company has been received",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @GetMapping("/companies/{id}")
    public final CompanyEntity getCompanyById(@PathVariable final Long id) {
        return (companyService.findByIdCompany(id))
                .orElseThrow(() -> new IllegalStateException("Company with this id was not found in the database."));
    }

    /**
     * This method return all companies.
     *
     * @return a list of {@link CompanyEntity} from database.
     */
    @Operation(summary = "This is to fetch all companies from the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Details of all companies.",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @GetMapping("/companies")
    public final List<CompanyEntity> getAllCompany() {
        return companyService.findAllCompany();
    }


    /**
     * Updates {@link CompanyEntity} in the database.
     *
     * @param id      the id of {@link CompanyEntity} that needs to update.
     * @param company the {@link CompanyEntity} that needs to update.
     * @throws IllegalStateException if the {@link CompanyEntity} with given id was not found in the database.
     */
    @Operation(summary = "This is update the company.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Company has been updated.",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @PutMapping("/companies/{id}")
    public final Optional<CompanyEntity> updatedCompany(@PathVariable final Long id, @RequestBody final CompanyEntity company) {
        companyService.findByIdCompany(id).orElseThrow(() -> new IllegalStateException("The company with id: " + id + " was not found in the database."));
        return companyService.updateCompanyById(id, company);
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
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
    })
    @DeleteMapping("/companies/{id}")
    public void deleteCompanyById(@PathVariable final Long id) {
        companyService.findByIdCompany(id).orElseThrow(() -> new IllegalStateException("The company with id: " + id + " was not found in the database."));
        companyService.deleteCompanyById(id);
    }


}
