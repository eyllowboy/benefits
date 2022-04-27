package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    Optional<CategoryEntity> findByTitle(final String title);

    @Query("FROM CategoryEntity cat JOIN FETCH cat.discounts WHERE cat.id = :id")
    Optional<CategoryEntity> findWithAssociatedDiscounts(@Param(value = "id") final Long id);

}
