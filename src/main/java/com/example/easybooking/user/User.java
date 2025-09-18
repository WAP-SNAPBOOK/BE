package com.example.easybooking.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kakaoId;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    public static User createNewUser(String kakaoId) {
        User user = new User();
        user.kakaoId = kakaoId;
        return user;
    }

    public enum Role {
        USER, ADMIN
    }
}
