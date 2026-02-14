package com.example.easybooking.availability.presentation;

import com.example.easybooking.availability.CustomerAvailabilityService;
import com.example.easybooking.availability.dto.response.AvailabilityMonthlyResponse;
import com.example.easybooking.availability.dto.response.AvailabilitySlotsResponse;
import java.time.LocalDate;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shops/{shopId}/staff/{staffId}/availability")
public class AvailabilityController {

    private final CustomerAvailabilityService customerAvailabilityService;

    @GetMapping
    public ResponseEntity<AvailabilitySlotsResponse> getAvailability(
            @PathVariable Long shopId,
            @PathVariable Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(customerAvailabilityService.getDailyAvailability(shopId, staffId, date));
    }

    @GetMapping("/monthly")
    public ResponseEntity<AvailabilityMonthlyResponse> getMonthlyAvailability(
            @PathVariable Long shopId,
            @PathVariable Long staffId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth
    ) {
        return ResponseEntity.ok(customerAvailabilityService.getMonthlyAvailability(shopId, staffId, yearMonth));
    }
}
