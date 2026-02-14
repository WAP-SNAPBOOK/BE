package com.example.easybooking.availability.presentation;

import com.example.easybooking.auth.annotation.RequireAuthenticatedUser;
import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.availability.ShopScheduleService;
import com.example.easybooking.availability.dto.request.CreateShopHolidayRequest;
import com.example.easybooking.availability.dto.request.UpdateShopOperatingTimesRequest;
import com.example.easybooking.availability.dto.request.UpdateShopScheduleSettingsRequest;
import com.example.easybooking.availability.dto.response.ShopHolidayResponse;
import com.example.easybooking.availability.dto.response.ShopHolidaysResponse;
import com.example.easybooking.availability.dto.response.ShopOperatingTimesResponse;
import com.example.easybooking.availability.dto.response.ShopScheduleSettingsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shops/{shopId}/schedule")
public class ShopScheduleController {

    private final ShopScheduleService shopScheduleService;

    @GetMapping("/settings")
    public ResponseEntity<ShopScheduleSettingsResponse> getSettings(
            @PathVariable Long shopId,
            @RequireAuthenticatedUser AuthenticatedUser user
    ) {
        return ResponseEntity.ok(shopScheduleService.getSettings(shopId, user.getUserId()));
    }

    @PutMapping("/settings")
    public ResponseEntity<ShopScheduleSettingsResponse> updateSettings(
            @PathVariable Long shopId,
            @Valid @RequestBody UpdateShopScheduleSettingsRequest request,
            @RequireAuthenticatedUser AuthenticatedUser user
    ) {
        return ResponseEntity.ok(shopScheduleService.updateSettings(shopId, user.getUserId(), request));
    }

    @PutMapping("/operating-times")
    public ResponseEntity<Void> updateOperatingTimes(
            @PathVariable Long shopId,
            @Valid @RequestBody UpdateShopOperatingTimesRequest request,
            @RequireAuthenticatedUser AuthenticatedUser user
    ) {
        shopScheduleService.updateOperatingTimes(shopId, user.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/operating-times")
    public ResponseEntity<ShopOperatingTimesResponse> getOperatingTimes(
            @PathVariable Long shopId,
            @RequireAuthenticatedUser AuthenticatedUser user
    ) {
        return ResponseEntity.ok(shopScheduleService.getOperatingTimes(shopId, user.getUserId()));
    }

    @GetMapping("/holidays")
    public ResponseEntity<ShopHolidaysResponse> getHolidays(
            @PathVariable Long shopId,
            @RequireAuthenticatedUser AuthenticatedUser user
    ) {
        return ResponseEntity.ok(shopScheduleService.getHolidays(shopId, user.getUserId()));
    }

    @PostMapping("/holidays")
    public ResponseEntity<ShopHolidayResponse> createHoliday(
            @PathVariable Long shopId,
            @Valid @RequestBody CreateShopHolidayRequest request,
            @RequireAuthenticatedUser AuthenticatedUser user
    ) {
        ShopHolidayResponse response = shopScheduleService.createHoliday(shopId, user.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/holidays/{holidayId}")
    public ResponseEntity<Void> deleteHoliday(
            @PathVariable Long shopId,
            @PathVariable Long holidayId,
            @RequireAuthenticatedUser AuthenticatedUser user
    ) {
        shopScheduleService.deleteHoliday(shopId, holidayId, user.getUserId());
        return ResponseEntity.noContent().build();
    }
}
