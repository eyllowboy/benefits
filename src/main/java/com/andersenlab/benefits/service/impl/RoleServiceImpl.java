package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.repository.RoleRepository;
import com.andersenlab.benefits.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * An implementation for performing operations on a {@link RoleEntity}.
 *
 * @author Andrei Rabchun
 * @version 1.0
 * @see RoleService
 */
@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(final RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Page<RoleEntity> findAll(final Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    @Override
    public Optional<RoleEntity> findById(final Long id) {
        return roleRepository.findById(id);
    }

    @Override
    public Optional<RoleEntity> findByCode(final String code) {
        return roleRepository.findByCode(code);
    }

    @Override
    public RoleEntity save(final RoleEntity role) {
        ValidateUtils.validateEntityPost(role);
        return roleRepository.save(role);
    }

    @Override
    public void updateRoleEntity(final Long id, final String name, final String code) {
        final RoleEntity role = new RoleEntity(id, name, code);
        ValidateUtils.validateEntityPatch(role);
        roleRepository.updateRoleEntity(role.getId(), role.getName(), role.getCode());
    }

    @Override
    public void delete(final Long id) {
        roleRepository.deleteById(id);
    }

    @Override
    public Optional<RoleEntity> findWithAssociatedUsers(final Long id) {
        return roleRepository.findWithAssociatedUsers(id);
    }
}
