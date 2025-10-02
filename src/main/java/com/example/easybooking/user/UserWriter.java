package com.example.easybooking.user;

import com.example.easybooking.user.domain.CustomerProfile;
import com.example.easybooking.user.domain.OwnerProfile;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
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

    @Transactional
    public User registerCustomer(CustomerSignUpRequest request,String providerId) {
        User user = User.createUser(providerId, request.getNickname(), UserType.CUSTOMER);
        User savedUser = userRepository.save(user);

        CustomerProfile customerProfile = CustomerProfile.createCustomerProfile(providerId);
        customerProfileRepository.save(customerProfile);

        return savedUser;
    }

    @Transactional
    public User registerOwner(OwnerSignUpRequest request, String providerId) {
        User user = User.createUser(providerId, request.getNickname(), UserType.OWNER);
        User savedUser = userRepository.save(user);

        OwnerProfile ownerProfile = OwnerProfile.createOwnerProfile(
                providerId,
                request.getBusinessName(),
                request.getBusinessNumber(),
                request.getAddress(),
                request.getPhoneNumber()
        );
        ownerProfileRepository.save(ownerProfile);

        return savedUser;
    }


    @Transactional
    public void deleteUser(String providerId) {
        customerProfileRepository.deleteByProviderId(providerId);
        ownerProfileRepository.deleteByProviderId(providerId);

        userRepository.deleteByProviderId(providerId);
    }
}
