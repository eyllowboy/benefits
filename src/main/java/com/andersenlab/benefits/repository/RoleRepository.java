package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
	RoleEntity findByCode(String code);
	
	@Modifying
	@Transactional
	@Query("update RoleEntity r set r.name = :name, r.code = :code where r.id = :id")
	void updateRoleEntity(@Param(value = "id") Long id,
	                      @Param(value = "name") String name,
	                      @Param(value = "code") String code);
}