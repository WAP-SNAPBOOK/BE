package com.example.easybooking.user;

import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.example.easybooking.user.domain.repository.UserRepository;
import com.example.easybooking.user.dto.CustomerSignUpRequest;
import com.example.easybooking.user.dto.OwnerSignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserWriter {
    private final UserRepository userRepository;
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User registerCustomer(CustomerSignUpRequest request,String providerId) {
        User user = User.createUser(providerId, request.getName(),request.getPhoneNumber(), UserType.CUSTOMER);
        User savedUser = userRepository.save(user);
        return savedUser;
    }

    @Transactional
    public User registerOwner(OwnerSignUpRequest request, String providerId) {
        User user = User.createUser(providerId, request.getName(),request.getPhoneNumber(), UserType.OWNER);
        User savedUser = userRepository.save(user);
        return savedUser;
    }


    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
