package com.example.easybooking.auth.dev.service;

import com.example.easybooking.auth.dev.DevAuthPersona;
import com.example.easybooking.auth.dev.dto.DevAuthPersonaResponse;
import com.example.easybooking.auth.dev.dto.DevAuthResetResponse;
import com.example.easybooking.auth.dto.AuthResponse;
import com.example.easybooking.auth.service.AuthService;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.service.UserCleanupService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"local", "test"})
@RequiredArgsConstructor
public class DevAuthService {
    private final AuthService authService;
    private final UserReader userReader;
    private final UserCleanupService userCleanupService;

    public AuthResponse login(String personaKey) {
        DevAuthPersona persona = DevAuthPersona.fromKey(personaKey);
        return authService.loginByProviderId(persona.getProviderId());
    }

    public List<DevAuthPersonaResponse> getPersonas() {
        return DevAuthPersona.all().stream()
                .map(persona -> DevAuthPersonaResponse.of(
                        persona,
                        userReader.getUserByProviderId(persona.getProviderId()).isPresent()
                ))
                .toList();
    }

    public DevAuthResetResponse resetPersona(String personaKey) {
        DevAuthPersona persona = DevAuthPersona.fromKey(personaKey);
        return userReader.getUserByProviderId(persona.getProviderId())
                .map(user -> {
                    userCleanupService.forceDeleteUser(user.getId());
                    return DevAuthResetResponse.deleted(persona.getKey());
                })
                .orElseGet(() -> DevAuthResetResponse.noOp(persona.getKey()));
    }
}
