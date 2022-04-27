package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.UserRepository;
import com.andersenlab.benefits.service.LocationService;
import com.andersenlab.benefits.service.RoleService;
import com.andersenlab.benefits.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;
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
    private final RoleService roleService;
    private final LocationService locationService;

    @Autowired
    public UserServiceImpl(final UserRepository userRepository, final RoleService roleService, final LocationService locationService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.locationService = locationService;
    }

    @Override
    public Page<UserEntity> findAll(final Pageable pageable) {
        return this.userRepository.findAll(pageable);
    }

    @Override
    public UserEntity findById(final Long id) {
        return this.userRepository.findById(id).orElseThrow(() ->
                new IllegalStateException("User with this id was not found in the database"));
    }

    @Override
    public Optional<UserEntity> findByLogin(final String login) {
        return this.userRepository.findByLogin(login);
    }

    @Override
    public UserEntity save(final UserEntity user) {
        user.setId(null);
        validateEntityFieldsAnnotations(user, true);
        findByLogin(user.getLogin()).ifPresent(foundUser -> {
                    throw new IllegalStateException("User with such 'login' is already exists");
                }
        );
        this.roleService.findById(user.getRoleEntity().getId());
        this.locationService.findById(user.getLocation().getId());
        return this.userRepository.save(user);
    }

    @Override
    public UserEntity update(final Long id, final UserEntity userEntity) {
        validateEntityFieldsAnnotations(userEntity, false);
        if (!Objects.isNull(userEntity.getRoleEntity()))
            this.roleService.findById(userEntity.getRoleEntity().getId());
        if (!Objects.isNull(userEntity.getLocation()))
            this.locationService.findById(userEntity.getLocation().getId());
        if (!Objects.isNull(userEntity.getLogin())) {
            final Optional<UserEntity> theSameUser = findByLogin(userEntity.getLogin());
            if (theSameUser.isPresent() && !theSameUser.get().getId().equals(id))
                throw new IllegalStateException("User with such 'login' is already exists");
        }
        final UserEntity existingUser = findById(userEntity.getId());
        BeanUtils.copyProperties(userEntity, existingUser, "id", "login");
        return this.userRepository.updateUserEntity(
                existingUser.getId(), existingUser.getLogin(), existingUser.getRoleEntity(), existingUser.getLocation());
    }

    @Override
    public void delete(final Long id) {
        this.findById(id);
        this.userRepository.deleteById(id);
    }
}
