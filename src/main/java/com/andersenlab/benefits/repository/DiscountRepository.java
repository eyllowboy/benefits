package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountRepository extends JpaRepository<DiscountEntity, Long>, JpaSpecificationExecutor<DiscountEntity> {
}
