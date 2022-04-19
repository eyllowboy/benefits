package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.UserRepository;
import com.andersenlab.benefits.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * An implementation for performing operations on a {@link UserEntity}.
 *
 * @author Andrei Rabchun
 * @version 1.0
 * @see UserService
 */
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Page<UserEntity> findAll(final Pageable pageable) {
        return this.userRepository.findAll(pageable);
    }

    @Override
    public Optional<UserEntity> findById(final Long id) {
        return this.userRepository.findById(id);
    }

    @Override
    public Optional<UserEntity> findByLogin(final String login) {
        return this.userRepository.findByLogin(login);
    }

    @Override
    public UserEntity save(final UserEntity user) {
        ValidateUtils.validateEntityPost(user);
        return this.userRepository.save(user);
    }

    @Override
    public void updateUserEntity(final Long id, final String login, final RoleEntity roleEntity, final LocationEntity location) {
        final UserEntity user = new UserEntity(id, login, roleEntity, location);
        ValidateUtils.validateEntityPatch(user);
        this.userRepository.updateUserEntity(user.getId(), user.getLogin(), user.getRoleEntity(), user.getLocation());
    }

    @Override
    public void delete(final Long id) {
        this.userRepository.deleteById(id);
    }
}
