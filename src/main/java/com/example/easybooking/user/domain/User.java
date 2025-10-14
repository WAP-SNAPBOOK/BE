package com.example.easybooking.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String providerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    public static User createUser(String providerId, String nickname,String phoneNumber,UserType userType) {
         User user = new User();
         user.providerId = providerId;
         user.name = nickname;
         user.phoneNumber = phoneNumber;
         user.userType = userType;
         return user;
    }

    public enum Role {
        USER, ADMIN
    }
}
