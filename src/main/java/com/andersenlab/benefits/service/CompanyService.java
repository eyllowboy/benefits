package com.andersenlab.benefits.service;


import com.andersenlab.benefits.domain.CompanyEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Main interface for performing basic operations on database {@link CompanyEntity}.
 *
 * @author Aleksei Sidorin
 * @version 1.0
 */

@Service
public interface CompanyService {

    /**
     * Method allows us finds all company.
     *
     * @return the list of companies from database, error if not processed
     */
    List<CompanyEntity> findAllCompany();

    /**
     * Method allows us find the company by id.
     *
     * @param id the id of record in the database, not null
     * @return the company corresponding given id from database, error if id not found
     */
    Optional<CompanyEntity> findByIdCompany(final Long id);

    /**
     * Method allows to create the new company.
     *
     * @param company contains information to create a new record in the database, not null
     * @return the entity corresponding new record in the database, error if consistency are not met
     */
    Optional<CompanyEntity> createCompany(final CompanyEntity company);

    /**
     * Method allows updates the company by id.
     *
     * @param id      the id of record in the database, not null
     * @param company contains information to the update record in the database, not null
     * @return the entity corresponding updated record in the database, error if consistency conditions are not met
     */
    Optional<CompanyEntity> updateCompanyById(final Long id, final CompanyEntity company);

    /**
     * Method allows deleted the company by id.
     *
     * @param id the id of record in the database, not null
     */
    void deleteCompanyById(final Long id);

    /**
     * Method allows get company with discount by the company id.
     *
     * @param id the id of record in the database, not null
     */
    Optional<CompanyEntity> findWithAssociatedDiscount(final Long id);
}
