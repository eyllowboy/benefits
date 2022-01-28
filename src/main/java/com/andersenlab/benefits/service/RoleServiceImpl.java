package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.RoleRepository;
import liquibase.pro.packaged.O;
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
    public List<RoleEntity> findAll() {
        return roleRepository.findAll();
    }
    
    @Override
    public Optional<RoleEntity> findById(Long id) {
        return roleRepository.findById(id);
    }
    
    @Override
    public Optional<RoleEntity> findByCode(String code) {
        return roleRepository.findByCode(code);
    }
    
    @Override
    public RoleEntity save(RoleEntity role) {
        return roleRepository.save(role);
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
