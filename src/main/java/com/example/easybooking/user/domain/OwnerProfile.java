package com.example.easybooking.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class OwnerProfile {

    @Id
    private String providerId;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false)
    private String businessNumber;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phoneNumber;

    public static OwnerProfile createOwnerProfile(String providerId, String businessName, String businessNumber, String address, String phoneNumber) {
         OwnerProfile profile = new OwnerProfile();
         profile.providerId = providerId;
         profile.businessName = businessName;
         profile.businessNumber = businessNumber;
         profile.address = address;
         profile.phoneNumber = phoneNumber;
         return profile;
    }
}
