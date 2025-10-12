package com.aslmk.authservice.service.impl;

import com.aslmk.authservice.entity.UserEntity;
import com.aslmk.authservice.repository.UserRepository;
import com.aslmk.authservice.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity create() {
        return userRepository.save(UserEntity.builder().build());
    }
}
