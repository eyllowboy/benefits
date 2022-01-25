package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    
    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    
    @Override
    public List<Optional<RoleEntity>> findAll() {
        return roleRepository.findAll()
                .stream()
                .map(x -> Optional.of(Objects.requireNonNullElseGet(x, RoleEntity::new)))
                .toList();
    }
    
    @Override
    public Optional<RoleEntity> findById(Long id) {
        return roleRepository.findById(id);
    }
    
    @Override
    public Optional<RoleEntity> findByCode(String code) {
        RoleEntity roleEntity = roleRepository.findByCode(code);
        if (roleEntity == null) {
            return Optional.empty();
        } else {
            return Optional.of(roleEntity);
        }
    }
    
    @Override
    public Optional<RoleEntity> save(RoleEntity role) {
        return Optional.of(roleRepository.save(role));
    }
    
    @Override
    public void updateRoleEntity(Long id, String name, String code) {
        roleRepository.updateRoleEntity(id, name, code);
    }
    
    @Override
    public void delete(Long id) {
        roleRepository.deleteById(id);
    }
}
