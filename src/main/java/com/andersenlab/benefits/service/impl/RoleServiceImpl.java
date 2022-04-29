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

import static com.andersenlab.benefits.service.impl.ValidateUtils.*;


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
                () -> new IllegalStateException(errIdNotFoundMessage("role", id)));
    }

    @Override
    public RoleEntity save(final RoleEntity role) {
        this.roleRepository.findByCode(role.getCode()).ifPresent(roleEntity -> {
                    throw new IllegalStateException(errAlreadyExistMessage("role", "role code", roleEntity.getCode()));
                }
        );
        role.setId(null);
        validateEntityFieldsAnnotations(role, true);
        return this.roleRepository.save(role);
    }

    @Override
    @Transactional
    public RoleEntity update(final Long id, final RoleEntity roleEntity) {
        if (!Objects.isNull(roleEntity.getCode())) {
            final Optional<RoleEntity> theSameCodeRole = this.roleRepository.findByCode(roleEntity.getCode());
            if (theSameCodeRole.isPresent() && (!theSameCodeRole.get().getId().equals(id))) {
                throw new IllegalStateException(errAlreadyExistMessage("role", "role code", roleEntity.getCode()));
            }
        }
        final RoleEntity existingRole = this.roleRepository.findById(id).orElseThrow(() ->
                new IllegalStateException(errIdNotFoundMessage("role", roleEntity.getId())));
        BeanUtils.copyProperties(roleEntity, existingRole, "id");
        final RoleEntity role = new RoleEntity(id, existingRole.getName(), existingRole.getCode());
        validateEntityFieldsAnnotations(role, false);
        return this.roleRepository.save(existingRole);
    }

    @Override
    public void delete(final Long id) {
        final RoleEntity existingRole = findById(id);
        final Optional<RoleEntity> roleEntity = this.findWithAssociatedUsers(id);
        if (roleEntity.isPresent() && roleEntity.get().getUsers().size() > 0) {
            throw new IllegalStateException(errAssociatedEntity("role", "discount"));
        }
        this.roleRepository.delete(existingRole);
    }

    @Override
    @Transactional
    public Optional<RoleEntity> findWithAssociatedUsers(final Long id) {
        return this.roleRepository.findWithAssociatedUsers(id);
    }
}
