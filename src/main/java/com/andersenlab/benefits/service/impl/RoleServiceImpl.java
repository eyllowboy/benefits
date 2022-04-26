package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.repository.RoleRepository;
import com.andersenlab.benefits.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.Optional;
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
        return this.roleRepository.findById(id).orElseThrow(
                () -> new IllegalStateException("Role with this id was not found in the database"));
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
    @Transactional
    public void updateRoleEntity(final Long id, final String name, final String code) {
        final RoleEntity role = new RoleEntity(id, name, code);
        validateEntityFieldsAnnotations(role, false);
        this.roleRepository.updateRoleEntity(role.getId(), role.getName(), role.getCode());
    }

    @Override
    public void delete(final Long id) {
        this.roleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Optional<RoleEntity> findWithAssociatedUsers(final Long id) {
        return this.roleRepository.findWithAssociatedUsers(id);
    }
}
