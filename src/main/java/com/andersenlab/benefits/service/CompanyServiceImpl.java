package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.CompanyEntity;
import com.andersenlab.benefits.repository.CompanyRepository;
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
    public CompanyServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public final List<CompanyEntity> findAllCompany() {
        return companyRepository.findAll();
    }

    @Override
    public final Optional<CompanyEntity> findByIdCompany(final Long id) {
        return companyRepository.findById(id);
    }

    @Override
    public final Optional<CompanyEntity> createCompany(final CompanyEntity company) {
        return Optional.of(companyRepository.save(company));
    }

    @Override
    public final Optional<CompanyEntity> updateCompanyById(final Long id, final CompanyEntity newCompany) {
        companyRepository.findById(id).map(company -> {
            company.setTitle(newCompany.getTitle());
            company.setDescription(newCompany.getDescription());
            company.setAddress(newCompany.getAddress());
            company.setPhone(newCompany.getPhone());
            company.setLink(newCompany.getLink());
            return companyRepository.save(company);
        }).orElseThrow(() -> {
            return new RuntimeException("The problem with updates company");
        });
        return Optional.of(newCompany);
    }

    @Override
    public void deleteCompanyById(final Long id) {
        companyRepository.deleteById(id);

    }
}
