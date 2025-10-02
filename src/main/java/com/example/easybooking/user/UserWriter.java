package com.example.easybooking.user;

import com.example.easybooking.user.domain.CustomerProfile;
import com.example.easybooking.user.domain.OwnerProfile;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.repository.CustomerProfileRepository;
import com.example.easybooking.user.domain.repository.OwnerProfileRepository;
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
    private final CustomerProfileRepository customerProfileRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    public User save(User user) {
        return userRepository.save(user);
    }

    public User registerCustomer(CustomerSignUpRequest request,String providerId) {
        User user = User.createUser(providerId, request.getNickname());
        CustomerProfile customerProfile = CustomerProfile.createCustomerProfile(providerId);
        customerProfileRepository.save(customerProfile);
        return userRepository.save(user);
    }

    public User registerOwner(OwnerSignUpRequest request, String providerId) {
        User user = User.createUser(providerId, request.getNickname());
        OwnerProfile ownerProfile = OwnerProfile.createOwnerProfile(
                providerId,request.getBusinessName(),
                request.getBusinessNumber(),
                request.getAddress(),
                request.getPhoneNumber());
        ownerProfileRepository.save(ownerProfile);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String providerId) {
        // CustomerProfile과 OwnerProfile 삭제 (providerId 기준)
        customerProfileRepository.deleteByProviderId(providerId);
        ownerProfileRepository.deleteByProviderId(providerId);

        // User 삭제
        userRepository.deleteByProviderId(providerId);
    }
}
