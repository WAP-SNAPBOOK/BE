package com.example.easybooking.user;

import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserReader {
    private final UserRepository userRepository;

    public Optional<User> getUserByProviderId(String kakaoId) {
        return userRepository.findByProviderId(kakaoId);
    }

    public User read(Long userId) {
        return userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("해당 유저가 없습니다."));
    }
}
