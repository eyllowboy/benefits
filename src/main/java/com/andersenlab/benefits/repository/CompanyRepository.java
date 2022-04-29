package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    Optional<CompanyEntity> findByTitle(final String title);

    @Query("FROM CompanyEntity c JOIN FETCH c.discounts WHERE c.id = :id")
    Optional<CompanyEntity> findWithAssociatedDiscounts(@Param(value = "id") final Long id);
}
