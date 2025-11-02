package com.example.easybooking.form.presentation;

import com.example.easybooking.auth.AuthenticatedUser;
import com.example.easybooking.auth.RequireAuthenticatedUser;
import com.example.easybooking.form.dto.request.FormPatchRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.easybooking.form.FormService;
import com.example.easybooking.form.dto.FormResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/form")
@Slf4j
public class FormController {

    private final FormService formService;

    @GetMapping("/{shopId}")
    public FormResponse getForm(
            @PathVariable Long shopId,
            @RequireAuthenticatedUser AuthenticatedUser authenticatedUser) {

        Long userId = authenticatedUser.getUserId();
        FormResponse formResponse = formService.getForm(shopId, userId);
        return formResponse;
    }

    @PatchMapping("/shops/{shopId}")
    public ResponseEntity<Void> patchForm(
            @PathVariable Long shopId,
            @RequireAuthenticatedUser AuthenticatedUser authenticatedUser,
            @Valid @RequestBody FormPatchRequest request
    ) {

        Long ownerUserId = authenticatedUser.getUserId();

        formService.patchForm(shopId, ownerUserId, request);

        return ResponseEntity.ok().build();
    }
}
