package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService extends CrudService<UserEntity> {
    Optional<UserEntity> findByLogin(final String login);
    void updateUserEntity(final Long id, final String login, final RoleEntity roleEntity);
}