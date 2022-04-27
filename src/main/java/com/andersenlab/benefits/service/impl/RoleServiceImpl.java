package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.repository.RoleRepository;
import com.andersenlab.benefits.service.RoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Objects;
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
        this.findByCode(role.getCode()).ifPresent(roleEntity -> {
            throw new IllegalStateException("Role with such 'code' is already exists");}
        );
        return this.roleRepository.save(role);
    }

    @Override
    @Transactional
    public RoleEntity update(final Long id, final RoleEntity roleEntity) {
        if (!Objects.isNull(roleEntity.getCode())) {
            final Optional<RoleEntity> theSameCodeRole = this.findByCode(roleEntity.getCode());
            if (theSameCodeRole.isPresent() && (!theSameCodeRole.get().getId().equals(id)))
                throw new IllegalStateException("Role with such 'code' is already exists");
        }
        final RoleEntity existingRole = this.findById(id);
        BeanUtils.copyProperties(roleEntity, existingRole, "id");
        final RoleEntity role = new RoleEntity(id, existingRole.getName(), existingRole.getCode());
        validateEntityFieldsAnnotations(role, false);
        this.roleRepository.updateRoleEntity(role.getId(), role.getName(), role.getCode());
        return role;
    }

    @Override
    public void delete(final Long id) {
        this.findById(id);
        final Optional<RoleEntity> roleEntity = this.findWithAssociatedUsers(id);
        if (roleEntity.isPresent() && roleEntity.get().getUsers().size() > 0)
            throw new IllegalStateException("There is active users with this Role in database");
        this.roleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Optional<RoleEntity> findWithAssociatedUsers(final Long id) {
        return this.roleRepository.findWithAssociatedUsers(id);
    }
}
