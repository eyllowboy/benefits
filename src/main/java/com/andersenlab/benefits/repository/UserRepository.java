package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
	UserEntity findByLogin(String login);
}