package com.example.easybooking.auth.dev.presentation;

import com.example.easybooking.auth.dev.dto.DevAuthLoginRequest;
import com.example.easybooking.auth.dev.dto.DevAuthPersonaResponse;
import com.example.easybooking.auth.dev.dto.DevAuthResetResponse;
import com.example.easybooking.auth.dev.service.DevAuthService;
import com.example.easybooking.auth.dto.AuthResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile({"local", "test"})
@RequiredArgsConstructor
@RequestMapping("/dev/auth")
public class DevAuthController {
    private final DevAuthService devAuthService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody DevAuthLoginRequest request) {
        return ResponseEntity.ok(devAuthService.login(request.personaKey()));
    }

    @GetMapping("/personas")
    public ResponseEntity<List<DevAuthPersonaResponse>> getPersonas() {
        return ResponseEntity.ok(devAuthService.getPersonas());
    }

    @PostMapping("/reset/persona/{personaKey}")
    public ResponseEntity<DevAuthResetResponse> resetPersona(@PathVariable String personaKey) {
        return ResponseEntity.ok(devAuthService.resetPersona(personaKey));
    }
}
