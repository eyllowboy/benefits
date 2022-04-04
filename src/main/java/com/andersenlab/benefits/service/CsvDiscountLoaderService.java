package com.andersenlab.benefits.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/***
 * Implementation for performing import CSV files
 * @author Denis Popov
 * @version 1.0
 */
@Service
public interface CsvDiscountLoaderService {
    /***
     * Method to load list of Discounts from CSV file. Failsafe.
     * @param file MultipartFile file contains contents of Discounts list
     * @param delimiter string separator which delimits columns of fields (default ";")
     * @return List of line-by-line separated result of import Discounts
     */
    List<String> loadDiscountsFromCsv(final MultipartFile file, final String delimiter);
}
