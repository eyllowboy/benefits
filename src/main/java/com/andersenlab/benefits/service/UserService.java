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


}

