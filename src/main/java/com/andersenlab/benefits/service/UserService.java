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
     */
    UserEntity createNewUser(final String login, final String password);

    /**
     * @param login the unique string representation to identify the {@link UserEntity}, not null
     * @return the {@link UserEntity} corresponding given login from database, error if the login not found
     */

}

