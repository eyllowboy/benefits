package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {
	RoleEntity findByCode(String code);
}
