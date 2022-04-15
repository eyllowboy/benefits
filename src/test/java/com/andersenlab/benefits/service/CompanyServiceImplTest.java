package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.CategoryEntity;
import com.andersenlab.benefits.domain.CompanyEntity;
import com.andersenlab.benefits.repository.CompanyRepository;
import com.andersenlab.benefits.service.impl.CompanyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "spring.main.lazy-initialization=true",
        classes = {CompanyService.class, CompanyServiceImpl.class})
class CompanyServiceImplTest {

    private final CompanyServiceImpl companyService;

    @MockBean
    private CompanyRepository companyRepository;

    @Autowired
    CompanyServiceImplTest(CompanyServiceImpl companyService) {
        this.companyService = companyService;
    }


    @Test
    void whenFindAllCompanyIsOk() {
        //given
        final List<CompanyEntity> companies = List.of(
                new CompanyEntity("Company1", "Description1", "Street1", "8900-00-00", "www.link1.ru"),
                new CompanyEntity("Company2", "Description2", "Street2", "8900-00-00", "www.link2.ru"),
                new CompanyEntity("Company3", "Description3", "Street3", "8900-00-00", "www.link3.ru"),
                new CompanyEntity("Company4", "Description4", "Street3", "8900-00-00", "www.link3.ru")
        );
        final Page<CompanyEntity> pageOfCompany = new PageImpl<>(companies);
        // when
        when(this.companyRepository.findAll(PageRequest.of(0,4))).thenReturn(pageOfCompany);
        final Page<CompanyEntity> foundCompany = this.companyService.findAllCompany(PageRequest.of(0,4));
        // then
        assertEquals(pageOfCompany, foundCompany);
        verify(this.companyRepository, times(1)).findAll(PageRequest.of(0,4));

    }

    @Test
    void whenFindByIdCompanyIsOk() {
        //given
        final CompanyEntity companyEntity = new CompanyEntity("Company1", "Description1", "Street1", "8900-00-00", "www.link.ru");

        //when
        when(companyRepository.findById(companyEntity.getId())).thenReturn(Optional.of(companyEntity));
        Optional<CompanyEntity> foundCompany = companyService.findByIdCompany(companyEntity.getId());

        //then
        assertEquals(Optional.of(companyEntity), foundCompany);
        verify(companyRepository, times(1)).findById(companyEntity.getId());
    }

    @Test
    void whenCreateCompanyIsOk() {
        //given
        final CompanyEntity companyEntity = new CompanyEntity("Company1", "Description1", "Street1", "8900-00-00", "www.link.ru");

        //when
        when(companyRepository.save(any(CompanyEntity.class))).thenReturn(companyEntity);
        final Optional<CompanyEntity> savedCompany = companyService.createCompany(companyEntity);

        //then
        assertEquals(Optional.of(companyEntity), savedCompany);
        verify(companyRepository, times(1)).save(any(CompanyEntity.class));

    }

    @Test
    void whenUpdateCompanyByIdIsOk() {
        //given
        final CompanyEntity companyEntity = new CompanyEntity("Company1", "Description1", "Street1", "8900-00-00", "www.link.ru");

        //when
        when(companyRepository.findById(companyEntity.getId())).thenReturn(Optional.of(companyEntity));
        Optional<CompanyEntity> foundCompany = companyService.findByIdCompany(companyEntity.getId());
        foundCompany.get().setTitle("Title");
        when(companyRepository.save(any(CompanyEntity.class))).thenReturn(foundCompany.get());
        companyService.createCompany(foundCompany.get());
        final Optional<CompanyEntity> updatedCompany = companyService.findByIdCompany(companyEntity.getId());

        //then
        assertThat(updatedCompany.get().getTitle()).isEqualTo("Title");
    }

    @Test
    void whenDeleteCompanyByIdIsOk() {
        //when
        companyService.deleteCompanyById(anyLong());

        //then
        verify(companyRepository, times(1)).deleteById(anyLong());
    }
}