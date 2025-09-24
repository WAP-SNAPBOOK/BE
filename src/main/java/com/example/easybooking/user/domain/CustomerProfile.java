package com.example.easybooking.user.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class CustomerProfile {
    @Id
    private String providerId;


    public static CustomerProfile createCustomerProfile(String providerId) {
         CustomerProfile profile = new CustomerProfile();
         profile.providerId = providerId;
         return profile;
    }
}
