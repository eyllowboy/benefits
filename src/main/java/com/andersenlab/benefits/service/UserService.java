package com.andersenlab.benefits.service;

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
     * @param  {@link UserEntity} stored in the database, not null
     * @return the {@link UserEntity}  from database, error if the login not found
     */
    UserEntity update(final Long id, final UserEntity userEntity);
}
