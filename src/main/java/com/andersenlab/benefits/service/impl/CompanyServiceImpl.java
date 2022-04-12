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
        return companyRepository.findAll();
    }

    @Override
    public Optional<CompanyEntity> findByIdCompany(final Long id) {
        return companyRepository.findById(id);
    }

    @Override
    public Optional<CompanyEntity> createCompany(final CompanyEntity company) {
        return Optional.of(companyRepository.save(company));
    }

    @Override
    public Optional<CompanyEntity> updateCompanyById(final Long id, final CompanyEntity newCompany) {
        companyRepository.findById(id).map(company -> {
            company.setTitle(newCompany.getTitle());
            company.setDescription(newCompany.getDescription());
            company.setAddress(newCompany.getAddress());
            company.setPhone(newCompany.getPhone());
            company.setLink(newCompany.getLink());
            return companyRepository.save(company);
        }).orElseThrow(() -> new RuntimeException("The problem with updates company"));
        return Optional.of(newCompany);
    }

    @Override
    public void deleteCompanyById(final Long id) {
        companyRepository.deleteById(id);
    }

    @Override
<<<<<<< HEAD
    public Optional<CompanyEntity> findWithAssociatedDiscount(final Long id) {
=======
    public Optional<CompanyEntity> findWithAssociatedDiscount(Long id) {
>>>>>>> 55eb5ba5b7b75130adf19340848937fc63e3d9ff
        return companyRepository.findWithAssociatedDiscounts(id);
    }

}
