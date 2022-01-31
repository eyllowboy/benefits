package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.RoleEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface RoleService extends CrudService<RoleEntity> {
    Optional<RoleEntity> findByCode(final String code);
    void updateRoleEntity(final Long id, final String name, final String code);
}
