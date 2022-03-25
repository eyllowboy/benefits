package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.CompanyEntity;
import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.domain.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CsvDiscountLoaderRepository extends JpaRepository<DiscountEntity, Long> {

    @Query("FROM CompanyEntity company WHERE company.title = :title")
    Optional<CompanyEntity> findCompanyByTitle(@Param(value = "title") String title);

    @Query("FROM LocationEntity location WHERE location.city = :city")
    Optional<LocationEntity> findLocationByCity(@Param(value = "city") String city);

    @Query("FROM DiscountEntity discount JOIN FETCH discount.company_id WHERE discount.company_id = :companyId")
    List<DiscountEntity> findDiscountByCompanyId(@Param(value = "companyId") CompanyEntity companyId);
}
