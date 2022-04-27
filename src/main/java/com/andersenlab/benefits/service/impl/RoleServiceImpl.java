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

import static com.andersenlab.benefits.service.impl.ValidateUtils.errIdNotFoundMessage;
import static com.andersenlab.benefits.service.impl.ValidateUtils.validateEntityFieldsAnnotations;

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
        return this.roleRepository.findAll(pageable);
    }

    @Override
    public RoleEntity findById(final Long id) {
        return this.roleRepository.findById(id).orElseThrow(() ->
                new IllegalStateException(errIdNotFoundMessage("Role", id)));
    }

    @Override
    public Optional<RoleEntity> findByCode(final String code) {
        return this.roleRepository.findByCode(code);
    }

    @Override
    public RoleEntity save(final RoleEntity role) {
        role.setId(null);
        validateEntityFieldsAnnotations(role, true);
        return this.roleRepository.save(role);
    }

    @Override
    public RoleEntity update(final Long id, final RoleEntity role) {
        validateEntityFieldsAnnotations(role, false);
        this.roleRepository.updateRoleEntity(role.getId(), role.getName(), role.getCode());
        return role;
    }

    @Override
    public void delete(final Long id) {
        this.roleRepository.deleteById(id);
    }

    @Override
    public Optional<RoleEntity> findWithAssociatedUsers(final Long id) {
        return this.roleRepository.findWithAssociatedUsers(id);
    }
}
