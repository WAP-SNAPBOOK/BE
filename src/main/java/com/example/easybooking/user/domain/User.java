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

    @Column(nullable = false)
    private String providerId;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    public static User createUser(String providerId, String nickname) {
         User user = new User();
         user.providerId = providerId;
         user.nickname = nickname;
         return user;
    }

    public enum Role {
        USER, ADMIN
    }
}
