package com.example.easybooking.auth.dev.dto;

import com.example.easybooking.auth.dev.DevAuthPersona;
import com.example.easybooking.user.domain.UserType;

public record DevAuthPersonaResponse(
        String personaKey,
        String providerId,
        UserType userType,
        String description,
        boolean signedUp
) {
    public static DevAuthPersonaResponse of(DevAuthPersona persona, boolean signedUp) {
        return new DevAuthPersonaResponse(
                persona.getKey(),
                persona.getProviderId(),
                persona.getUserType(),
                persona.getDescription(),
                signedUp
        );
    }
}
