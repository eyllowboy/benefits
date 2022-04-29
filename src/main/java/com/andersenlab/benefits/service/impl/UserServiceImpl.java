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

import java.util.Objects;
import java.util.Optional;

import static com.andersenlab.benefits.service.impl.ValidateUtils.*;

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
    public UserServiceImpl(final UserRepository userRepository, final RoleRepository roleRepository, final LocationRepository locationRepository) {
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
        return this.userRepository.findById(id).orElseThrow(() ->
                new IllegalStateException(errIdNotFoundMessage("user", id)));
    }

    @Override
    public UserEntity save(final UserEntity entity) {

        this.userRepository.findByLogin(entity.getLogin()).ifPresent(foundUser -> {
                    throw new IllegalStateException(errAlreadyExistMessage("user", "user login", entity.getLogin()));
                }
        );
        this.roleRepository.findById(entity.getRoleEntity().getId()).orElseThrow(() -> {
                    throw new IllegalStateException(errIdNotFoundMessage("role", entity.getRoleEntity().getId()));
                }
        );
        this.locationRepository.findById(entity.getLocation().getId()).orElseThrow(() -> {
                    throw new IllegalStateException(errIdNotFoundMessage("location", entity.getLocation().getId()));
                }
        );
        entity.setId(null);
        validateEntityFieldsAnnotations(entity, true);
        return this.userRepository.save(entity);
    }

    @Override
    public UserEntity update(final Long id, final UserEntity userEntity) {

        if (!Objects.isNull(userEntity.getRoleEntity())) {
            this.roleRepository.findById(userEntity.getRoleEntity().getId()).orElseThrow(() ->
                    new IllegalStateException(errIdNotFoundMessage("role", userEntity.getRoleEntity().getId())));
        }
        if (!Objects.isNull(userEntity.getLocation())) {
            this.locationRepository.findById(userEntity.getLocation().getId()).orElseThrow(() ->
                    new IllegalStateException(errIdNotFoundMessage("location", userEntity.getLocation().getId())));
        }
        if (!Objects.isNull(userEntity.getLogin())) {
            final Optional<UserEntity> theSameUser = this.userRepository.findByLogin(userEntity.getLogin());
            if (theSameUser.isPresent() && !theSameUser.get().getId().equals(id)) {
                throw new IllegalStateException(errAlreadyExistMessage("user", "user login", userEntity.getLogin()));
            }
        }
        final UserEntity existingUser = this.userRepository.findById(userEntity.getId()).orElseThrow(() ->
                new IllegalStateException(errIdNotFoundMessage("user", userEntity.getId())));
        BeanUtils.copyProperties(userEntity, existingUser, "id", "login");
        validateEntityFieldsAnnotations(userEntity, false);
        return this.userRepository.save(existingUser);
    }

    @Override
    public void delete(final Long id) {
        final UserEntity existingUser = findById(id);
        this.userRepository.delete(existingUser);
    }
}
