package com.example.easybooking.user.presentation;

import com.example.easybooking.auth.annotation.RequireAuthenticatedUser;
import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.user.service.UserCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
public class DevController {
    private final UserCleanupService userCleanupService;

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(
            @RequireAuthenticatedUser AuthenticatedUser user) {
        userCleanupService.forceDeleteUser(user.getUserId());
        return ResponseEntity.ok("회원 및 연관 데이터가 모두 삭제되었습니다.");
    }

    @DeleteMapping("/dev/user/{userId}")
    public ResponseEntity<String> deleteUserInDev(
            @RequireAuthenticatedUser AuthenticatedUser user,
            @PathVariable Long userId) {
        userCleanupService.forceDeleteUser(userId);
        return ResponseEntity.ok("회원 및 연관 데이터가 모두 삭제되었습니다.");
    }
}



