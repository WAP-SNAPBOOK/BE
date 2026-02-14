package com.example.easybooking.availability.presentation;

import com.example.easybooking.auth.annotation.RequireAuthenticatedUser;
import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.availability.StaffOperatingTimesService;
import com.example.easybooking.availability.dto.request.UpdateStaffOperatingTimesRequest;
import com.example.easybooking.availability.dto.response.StaffOperatingTimesResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shops/{shopId}/staff/{staffId}/operating-times")
public class StaffOperatingTimesController {

    private final StaffOperatingTimesService staffOperatingTimesService;

    @PutMapping
    public ResponseEntity<Void> updateStaffOperatingTimes(
            @PathVariable Long shopId,
            @PathVariable Long staffId,
            @Valid @RequestBody UpdateStaffOperatingTimesRequest request,
            @RequireAuthenticatedUser AuthenticatedUser user
    ) {
        staffOperatingTimesService.updateStaffOperatingTimes(shopId, staffId, user.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<StaffOperatingTimesResponse> getStaffOperatingTimes(
            @PathVariable Long shopId,
            @PathVariable Long staffId,
            @RequireAuthenticatedUser AuthenticatedUser user
    ) {
        return ResponseEntity.ok(staffOperatingTimesService.getStaffOperatingTimes(shopId, staffId, user.getUserId()));
    }
}
