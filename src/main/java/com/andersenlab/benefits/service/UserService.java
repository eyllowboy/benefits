package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.UserEntity;
import org.springframework.stereotype.Service;

/**
 * An interface for performing unique operations on a {@link UserEntity}.
 * @see CrudService
 * @author Andrei Rabchun
 * @version 1.0
 */
@Service
public interface UserService extends CrudService<UserEntity> {

    /**
     * Method to create new {@link UserEntity}
     * @param login the unique string representation to identify the {@link UserEntity}, not null
     * @param password of new User
     * @throws IllegalStateException if user with the same login already exists
     * @return created {@link UserEntity} with Role ROLE_USER and Location "Белоруссия/Минск"
     */
    UserEntity createNewUser(final String login, final String password);
}
