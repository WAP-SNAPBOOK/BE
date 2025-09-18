package com.example.easybooking.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserWriter {
    private final UserRepository userRepository;
    public User save(User user) {
        return userRepository.save(user);
    }
}
