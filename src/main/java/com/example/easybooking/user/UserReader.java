package com.example.easybooking.user;

import com.example.easybooking.user.domain.OwnerProfile;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.repository.CustomerProfileRepository;
import com.example.easybooking.user.domain.repository.OwnerProfileRepository;
import com.example.easybooking.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserReader {
    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final OwnerProfileRepository ownerProfileRepository;

    public Optional<User> getUserByProviderId(String kakaoId) {
        return userRepository.findByProviderId(kakaoId);
    }
    public OwnerProfile getOwnerProfile(String kakaoId) {return ownerProfileRepository.findByProviderId(kakaoId);}
}
