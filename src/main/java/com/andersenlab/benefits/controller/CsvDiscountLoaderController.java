package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.service.CsvDiscountLoaderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

/**
 * A controller for uploading entries from csv-file to the database.
 * @author Andrei Rabchun
 * @version 1.0
 */
@ApiResponses(value = {
        @ApiResponse(responseCode = "200",
                description = "File has been uploaded",
                content = @Content),
        @ApiResponse(responseCode = "400",
                description = "Bad Request",
                content = @Content),
        @ApiResponse(responseCode = "401",
                description = "Unauthorized",
                content = @Content),
        @ApiResponse(responseCode = "403",
                description = "Forbidden",
                content = @Content),
        @ApiResponse(responseCode = "415",
                description = "Unsupported Media Type",
                content = @Content),
        @ApiResponse(responseCode = "500",
                description = "Internal Server Error",
                content = @Content)
})
@Tag(name = "Upload controller", description = "Controller for uploading data about discounts in the database.")
@RestController
@SecurityRequirement(name = "benefits")
public class CsvDiscountLoaderController {

    private final CsvDiscountLoaderService csvDiscountLoaderService;

    @Autowired
    public CsvDiscountLoaderController(final CsvDiscountLoaderService csvDiscountLoaderService) {
        this.csvDiscountLoaderService = csvDiscountLoaderService;
    }

    /**
     * Upload information in the database.
     * @param file the csv file to upload
     * @param delimiter string delimiter of columns
     * @throws IllegalStateException if:
     * <ul>
     * <li>a CSV file hasn't got
     * </ul>
     */
    @Operation(summary = "This is to upload entries")
    @PostMapping(value = "/upload-csv-file", consumes = {"multipart/form-data"})
    public List<String> uploadCsvFile(@RequestParam(name = "file") final MultipartFile file,
                                      @RequestParam(name = "delimiter", defaultValue = ";") final String delimiter) {
        if (file.isEmpty() || !Objects.requireNonNull(file.getOriginalFilename()).endsWith(".csv")) {
            throw new IllegalStateException("Please select a CSV file to upload");
        } else {
            return this.csvDiscountLoaderService.loadDiscountsFromCsv(file, "\\" + delimiter);
        }
    }
    
}
