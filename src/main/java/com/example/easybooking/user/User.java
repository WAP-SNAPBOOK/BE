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

    private String nickname;

    private String providerId;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    public static User createNewUser(String providerId, String nickname) {
         User user = new User();
         user.providerId = providerId;
         user.nickname = nickname;
         return user;
    }

    public enum Role {
        USER, ADMIN
    }
}
