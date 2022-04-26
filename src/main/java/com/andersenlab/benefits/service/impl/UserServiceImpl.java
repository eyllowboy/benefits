package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.LocationRepository;
import com.andersenlab.benefits.repository.RoleRepository;
import com.andersenlab.benefits.repository.UserRepository;
import com.andersenlab.benefits.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.andersenlab.benefits.service.impl.ValidateUtils.validateEntityFieldsAnnotations;

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
    private final RoleRepository roleRepository;
    private final LocationRepository locationRepository;

    @Autowired
    public UserServiceImpl(final UserRepository userRepository,
                           final RoleRepository roleRepository,
                           final LocationRepository locationRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public Page<UserEntity> findAll(final Pageable pageable) {
        return this.userRepository.findAll(pageable);
    }

    @Override
    public UserEntity findById(final Long id) {
        return this.userRepository.findById(id).orElseThrow(
                () -> new IllegalStateException("User with this id was not found in the database"));
    }

    @Override
    public Optional<UserEntity> findByLogin(final String login) {
        return this.userRepository.findByLogin(login);
    }

    @Override
    public UserEntity save(final UserEntity user) {
        this.userRepository.findByLogin(user.getLogin()).ifPresent(foundUser -> {
                    throw new IllegalStateException("User with such 'login' is already exists");
                }
        );
        this.roleRepository.findById(user.getRoleEntity().getId()).orElseThrow(() -> {
                    throw new IllegalStateException("Role with this id was not found in the database");
                }
        );
        this.locationRepository.findById(user.getLocation().getId()).orElseThrow(() -> {
                    throw new IllegalStateException("Location with this id was not found in the database");
                }
        );
        user.setId(null);
        validateEntityFieldsAnnotations(user, true);
        return this.userRepository.save(user);
    }

    @Override
    @Transactional
    public UserEntity update(final Long id, final UserEntity userEntity) {
        final UserEntity existingUser = this.userRepository.findById(userEntity.getId()).orElseThrow(() ->
                new IllegalStateException("User with this id was not found in the database"));
        this.roleRepository.findById(userEntity.getRoleEntity().getId()).orElseThrow(() ->
                new IllegalStateException("Role with this id was not found in the database"));
        this.userRepository.findByLogin(userEntity.getLogin()).ifPresent(foundUser -> {
            throw new IllegalStateException("User with such 'login' is already exists");
        });
        this.locationRepository.findById(userEntity.getLocation().getId()).orElseThrow(() ->
                new IllegalStateException("Location with this id was not found in the database"));
        BeanUtils.copyProperties(userEntity, existingUser, "id");
        final UserEntity user = new UserEntity(id, existingUser.getLogin(), existingUser.getRoleEntity(), existingUser.getLocation());
        validateEntityFieldsAnnotations(user, false);
        return this.userRepository.updateUserEntity(user.getId(), user.getLogin(), user.getRoleEntity(), user.getLocation());
    }

    @Override
    public void delete(final Long id) {
        this.userRepository.deleteById(id);
    }
}
