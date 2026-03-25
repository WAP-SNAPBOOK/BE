package com.example.easybooking.auth.dev.dto;

public record DevAuthResetResponse(
        String personaKey,
        boolean deleted,
        String message
) {
    public static DevAuthResetResponse deleted(String personaKey) {
        return new DevAuthResetResponse(personaKey, true, "persona user deleted");
    }

    public static DevAuthResetResponse noOp(String personaKey) {
        return new DevAuthResetResponse(personaKey, false, "persona user not found");
    }
}
