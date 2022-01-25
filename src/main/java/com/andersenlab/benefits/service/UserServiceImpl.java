package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public List<Optional<UserEntity>> findAll() {
        return userRepository.findAll()
                .stream()
                .map(x -> Optional.of(Objects.requireNonNullElseGet(x, UserEntity::new)))
                .toList();
    }
    
    @Override
    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    public Optional<UserEntity> findByLogin(String login) {
        UserEntity userEntity = userRepository.findByLogin(login);
        if (userEntity == null) {
            return Optional.empty();
        } else {
            return Optional.of(userEntity);
        }
    }
    
    @Override
    public Optional<UserEntity> save(UserEntity employee) {
        if (employee.getRoleEntity() != null) {
            return Optional.of(userRepository.save(employee));
        }
        
        throw new IllegalStateException("Role must be specified");
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
