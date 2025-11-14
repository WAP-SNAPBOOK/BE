package com.example.easybooking.user;

import com.example.easybooking.errors.errorcode.UserErrorCode;
import com.example.easybooking.errors.exception.UserException;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserReader {
    private final UserRepository userRepository;

    public Optional<User> getUserByProviderId(String kakaoId) {
        return userRepository.findByProviderId(kakaoId);
    }

    public User read(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }
}
