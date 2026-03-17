package com.aslmk.authservice.service.user;

import com.aslmk.authservice.domain.user.UserEntity;
import com.aslmk.authservice.repository.UserRepository;
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
