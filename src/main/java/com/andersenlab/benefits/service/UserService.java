package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * An interface for performing unique operations on a {@link UserEntity}.
 * @see CrudService
 * @author Andrei Rabchun
 * @version 1.0
 */
@Service
public interface UserService extends CrudService<UserEntity> {
    
    /**
     * @param login the unique string representation to identify the {@link UserEntity}, not null
     * @return the {@link UserEntity} corresponding given login from database, error if the login not found
     */
    Optional<UserEntity> findByLogin(final String login);
    
    /**
     * @param id the id of {@link UserEntity} in the database, not null
     * @param login the login of {@link UserEntity} stored in the database, not null
     * @param roleEntity the {@link RoleEntity} assigned to the {@link UserEntity}, not null
     */
    void updateUserEntity(final Long id, final String login, final RoleEntity roleEntity);
}