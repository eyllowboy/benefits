package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.CompanyEntity;
import com.andersenlab.benefits.repository.CompanyRepository;
import com.andersenlab.benefits.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * The implementation for performing operations on a {@link CompanyEntity}
 *
 * @author Aleksei Sidorin
 * @version 1.0
 * @see CompanyEntity
 * @see CompanyService
 */

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyServiceImpl(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public List<CompanyEntity> findAllCompany() {
        return this.companyRepository.findAll();
    }

    @Override
    public Optional<CompanyEntity> findByIdCompany(final Long id) {
        return this.companyRepository.findById(id);
    }

    @Override
    public Optional<CompanyEntity> createCompany(final CompanyEntity company) {
        ValidateUtils.validateEntityPost(company);
        return Optional.of(this.companyRepository.save(company));
    }

    @Override
    public Optional<CompanyEntity> updateCompanyById(final Long id, final CompanyEntity newCompany) {
        ValidateUtils.validateEntityPatch(newCompany);
        return Optional.of(this.companyRepository.save(newCompany));
    }

    @Override
    public void deleteCompanyById(final Long id) {
<<<<<<< HEAD
        this.companyRepository.deleteById(id);
    }

    @Override
    public Optional<CompanyEntity> findWithAssociatedDiscount(final Long id) {
        return this.companyRepository.findWithAssociatedDiscounts(id);
=======
        companyRepository.deleteById(id);
    }

    @Override
    public Optional<CompanyEntity> findWithAssociatedDiscount(Long id) {
        return companyRepository.findWithAssociatedDiscounts(id);
>>>>>>> 55eb5ba (Added toString method to Role, Location, Company, Category with exclude Set<> of associated entities (lazy init). Edited @EqualsAndHashCode to exclude Id and Set<> dependency)
    }

}
