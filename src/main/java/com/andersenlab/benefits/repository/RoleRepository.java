package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByCode(final String code);

    @Query("from RoleEntity r join fetch r.users where r.id = :id")
    Optional<RoleEntity> findWithAssociatedUsers(@Param(value = "id") final Long id);
}