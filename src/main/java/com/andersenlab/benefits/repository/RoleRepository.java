package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByCode(final String code);
    Optional<RoleEntity> findByName(final String name);

    @Modifying
    @Transactional
    @Query("update RoleEntity r set r.name = :name, r.code = :code where r.id = :id")
    void updateRoleEntity(@Param(value = "id") final Long id,
                          @Param(value = "name") final String name,
                          @Param(value = "code") final String code);

    @Transactional
    @Query("from RoleEntity r join fetch r.users where r.id = :id")
    Optional<RoleEntity> findWithAssociatedUsers(@Param(value = "id") final Long id);
}