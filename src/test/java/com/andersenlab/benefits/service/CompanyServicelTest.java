package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.CompanyEntity;
import com.andersenlab.benefits.repository.CompanyRepository;
import com.andersenlab.benefits.service.impl.CompanyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.andersenlab.benefits.service.ServiceTestUtils.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "spring.main.lazy-initialization=true",
        classes = {CompanyService.class, CompanyServiceImpl.class})
class CompanyServicelTest {
    private final CompanyServiceImpl companyService;
    private final List<CompanyEntity> companies = new ArrayList<>();

    @MockBean
    private CompanyRepository companyRepository;

    @Autowired
    CompanyServicelTest(final CompanyServiceImpl companyService) {
        this.companyService = companyService;
    }

    @BeforeEach
    public void ResetData() {
        when(this.companyRepository.findAll(any(Pageable.class))).thenAnswer(invocation ->
            new PageImpl<>(this.companies, invocation.getArgument(0), 100));
        when(this.companyRepository.findAll()).thenReturn(this.companies);
        when(this.companyRepository.findById(anyLong())).thenAnswer(invocation ->
                this.companies.stream().filter(company ->
                        Objects.equals(company.getId(), invocation.getArgument(0))).findFirst());
        when(this.companyRepository.save(any(CompanyEntity.class))).thenAnswer(invocation -> {
            final CompanyEntity savedCompany = invocation.getArgument(0);
            savedCompany.setId((long) this.companies.size());
            this.companies.add(savedCompany);
            return invocation.getArgument(0);
        });
        when(this.companyRepository.saveAll(anyList())).thenAnswer(invocation -> {
            final List<CompanyEntity> companiesToSave = invocation.getArgument(0);
            companiesToSave.forEach(item -> saveItem(this.companies, item, ServiceTestUtils::isCompaniesEquals));
            return companiesToSave;
        });
        doAnswer(invocation -> this.companies.remove((CompanyEntity) invocation.getArgument(0)))
                .when(this.companyRepository).delete(any(CompanyEntity.class));
        this.companies.clear();
        this.companyRepository.saveAll(getCompanyList());
    }

    @Test
    void whenFindAllSuccess() {
        //given
        final Page<CompanyEntity> pageOfCompany = new PageImpl<>(this.companies);

        // when
        final Page<CompanyEntity> foundCompany = this.companyService.findAll(PageRequest.of(0, 4));

        // then
        assertEquals(pageOfCompany.stream().toList(), foundCompany.getContent().stream().toList());
        verify(this.companyRepository, times(1)).findAll(PageRequest.of(0, 4));
    }

    @Test
    void whenFindByIdSuccess() {
        //given
        final CompanyEntity company = this.companies.get(getRndEntityPos() - 1);

        //when
        final CompanyEntity foundCompany = this.companyService.findById(company.getId());

        //then
        assertEquals(company, foundCompany);
        verify(this.companyRepository, times(1)).findById(company.getId());
    }

    @Test
    void whenSaveSuccess() {
        //given
        final int sizeBeforeSave = this.companies.size();
        final CompanyEntity company = getCompany(getRndEntityPos());

        //when
        final CompanyEntity savedCompany = this.companyService.save(company);

        //then
        assertEquals(company, savedCompany);
        assertEquals(sizeBeforeSave + 1, this.companyRepository.findAll().size());
        verify(this.companyRepository, times(1)).save(company);
    }

    @Test
    void whenUpdateSuccess() {
        //given
        final CompanyEntity company = this.companies.get(getRndEntityPos() - 1);
        company.setTitle("Title");

        //when
        final CompanyEntity updatedCompany = this.companyService.update(company.getId(), company);

        //then
        assertThat(updatedCompany.getTitle()).isEqualTo("Title");
    }

    @Test
    void whenDeleteCompanySuccess() {
        final int sizeBeforeDelete = this.companies.size();
        final CompanyEntity company = this.companies.get(getRndEntityPos() - 1);

        //when
        this.companyService.delete(company.getId());

        //then
        assertEquals(sizeBeforeDelete - 1, this.companyRepository.findAll().size());
        assertEquals(Optional.empty(), this.companyRepository.findById(company.getId()));
        verify(this.companyRepository, times(1)).delete(company);
    }
}