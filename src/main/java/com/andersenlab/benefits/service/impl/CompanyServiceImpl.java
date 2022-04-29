package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.CompanyEntity;
import com.andersenlab.benefits.repository.CompanyRepository;
import com.andersenlab.benefits.service.CompanyService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.Optional;
import static com.andersenlab.benefits.service.impl.ValidateUtils.*;


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
    public CompanyEntity findById(final Long id) {
        return this.companyRepository.findById(id).orElseThrow(() ->
                new IllegalStateException(errIdNotFoundMessage("Company", id)));
    }

    @Override
    public Page<CompanyEntity> findAll(final Pageable pageable) {
        return this.companyRepository.findAll(pageable);
    }

    @Override
    public CompanyEntity update(final Long id, final CompanyEntity company) {
        if (!Objects.isNull(company.getTitle())) {
            this.companyRepository.findByTitle(company.getTitle()).ifPresent(foundCompany -> {
                throw new IllegalStateException(
                    errAlreadyExistMessage("Company", "title", company.getTitle()));});
        }
        final CompanyEntity existingCompany = findById(id);
        BeanUtils.copyProperties(company, existingCompany, "id");
        validateEntityFieldsAnnotations(company, false);
        return this.companyRepository.save(company);
    }

    @Override
    public CompanyEntity save(final CompanyEntity company) {
        this.companyRepository.findByTitle(company.getTitle()).ifPresent(foundCompany -> {
            throw new IllegalStateException(
                    errAlreadyExistMessage("Company", "title", company.getTitle()));}
        );
        company.setId(null);
        validateEntityFieldsAnnotations(company, true);
        return this.companyRepository.save(company);
    }

    @Override
    public void delete(final Long id) {
        final CompanyEntity existingCompany = findById(id);
        final Optional<CompanyEntity> company = this.companyRepository.findWithAssociatedDiscounts(id);
        if (company.isPresent() && company.get().getDiscounts().size() > 0) {
            throw new IllegalStateException(
                    errAssociatedEntity("discounts", "Company"));
        }
        this.companyRepository.delete(existingCompany);
    }
}
