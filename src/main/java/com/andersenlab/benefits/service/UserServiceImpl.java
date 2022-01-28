package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }
    
    @Override
    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    public Optional<UserEntity> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }
    
    @Override
    public UserEntity save(UserEntity employee) {
            return userRepository.save(employee);
    }
    
    @Override
    public void updateUserEntity(Long id, String login, RoleEntity roleEntity) {
        userRepository.updateUserEntity(id, login, roleEntity);
    }
    
    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
