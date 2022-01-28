package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
	Optional<UserEntity> findByLogin(String login);
	
	@Modifying
	@Transactional
	@Query("update UserEntity u set u.login = :login, u.roleEntity = :roleEntity where u.id = :id")
	void updateUserEntity(@Param(value = "id") Long id,
	                      @Param(value = "login") String login,
	                      @Param(value = "roleEntity") RoleEntity roleEntity);
}