package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.LocationEntity;
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
    Optional<UserEntity> findByLogin(final String login);

    @Modifying
    @Query("update UserEntity u set u.login = :login, u.roleEntity = :roleEntity, u.location = :location where u.id = :id")
    UserEntity updateUserEntity(@Param(value = "id") final Long id,
                          @Param(value = "login") final String login,
                          @Param(value = "roleEntity") final RoleEntity roleEntity,
                          @Param(value = "location") final LocationEntity location);
}