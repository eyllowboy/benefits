package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<UserEntity> findById(final Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<UserEntity> findByLogin(final String login) {
        return userRepository.findByLogin(login);
    }

    @Override
    public UserEntity save(final UserEntity employee) {
        return userRepository.save(employee);
    }

    @Override
    public void updateUserEntity(final Long id, final String login, final RoleEntity roleEntity) {
        userRepository.updateUserEntity(id, login, roleEntity);
    }

    @Override
    public void delete(final Long id) {
        userRepository.deleteById(id);
    }
}
