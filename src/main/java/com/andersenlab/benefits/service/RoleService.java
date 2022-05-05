package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * An interface for performing unique operations on a {@link RoleEntity}.
 * @see CrudService
 * @author Andrei Rabchun
 * @version 1.0
 */
@Service
public interface RoleService extends CrudService<RoleEntity> {

    /**
     * @param id the id of {@link RoleEntity} in the database, not null
     * @param {@link RoleEntity} stored in the database, not null
     * @return {@link RoleEntity} corresponding given code from database, error if the code not found
     */
    RoleEntity update(final Long id, final RoleEntity roleEntity);

    /**
     * Method to get {@link RoleEntity} with EAGER fetch associated {@link UserEntity}
     * @param id the id of {@link RoleEntity} need to load, not null
     * @return {@link RoleEntity} with given id, error if id role found
     */
    Optional<RoleEntity> findWithAssociatedUsers(final Long id);
}
