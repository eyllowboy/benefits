package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.RoleRepository;
import com.andersenlab.benefits.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    
    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }
    
    public UserEntity findById(Integer id) {
        return userRepository.findById(id).get();
    }
    
    public UserEntity findByLogin(String login) {
        return userRepository.findByLogin(login);
    }
    
    public UserEntity save(UserEntity employee) {
        if (employee.getRoleEntity() != null) {
            return userRepository.save(employee);
        }
        
        throw new BenefitsServiceException();
    }

    public void updateUser(UserEntity user) {
        userRepository.save(user);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
}
