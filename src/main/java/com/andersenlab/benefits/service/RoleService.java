package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    private RoleRepository roleRepository;
    
    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    
    public List<RoleEntity> findAll() {
        return roleRepository.findAll();
    }
    
    public RoleEntity findById(Integer id) {
        return roleRepository.findById(id).get();
    }
    
    public RoleEntity findByCode(String code) {
        return roleRepository.findByCode(code);
    }
    
    public RoleEntity save(RoleEntity role) {
        return roleRepository.save(role);
    }
    
    public void deleteRole(Integer id) {
        roleRepository.deleteById(id);
    }
}
