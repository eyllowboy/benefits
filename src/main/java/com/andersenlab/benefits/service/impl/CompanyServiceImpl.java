package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.CompanyEntity;
import com.andersenlab.benefits.repository.CompanyRepository;
import com.andersenlab.benefits.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.andersenlab.benefits.service.impl.ValidateUtils.validateEntityFieldsAnnotations;

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
    public Page<CompanyEntity> findAllCompany(final Pageable pageable) {
        return this.companyRepository.findAll(pageable);
    }

    @Override
    public Optional<CompanyEntity> findByIdCompany(final Long id) {
        return this.companyRepository.findById(id);
    }

    @Override
    public Optional<CompanyEntity> findByTitle(final String title) {
        return this.companyRepository.findCompanyEntityByTitle(title);
    }

    @Override
    public Optional<CompanyEntity> createCompany(final CompanyEntity company) {
        company.setId(null);
        validateEntityFieldsAnnotations(company, true);
        return Optional.of(this.companyRepository.save(company));
    }

    @Override
    public Optional<CompanyEntity> updateCompanyById(final Long id, final CompanyEntity newCompany) {
        validateEntityFieldsAnnotations(newCompany, false);
        return Optional.of(this.companyRepository.save(newCompany));
    }

    @Override
    public void deleteCompanyById(final Long id) {
        this.companyRepository.deleteById(id);
    }

    @Override
    public Optional<CompanyEntity> findWithAssociatedDiscount(final Long id) {
        return this.companyRepository.findWithAssociatedDiscounts(id);
    }
}
