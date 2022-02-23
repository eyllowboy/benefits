package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;


@Repository
<<<<<<< HEAD
public interface DiscountRepository extends JpaRepository<DiscountEntity, Long>, JpaSpecificationExecutor<DiscountEntity> {
=======
public interface DiscountRepository extends JpaRepository<DiscountEntity, Long> {
>>>>>>> 9599459 (Discount modified, tables created, tests not working)


}
