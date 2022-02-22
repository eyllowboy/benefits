package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
}
