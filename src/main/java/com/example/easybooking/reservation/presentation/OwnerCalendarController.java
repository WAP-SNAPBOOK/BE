package com.example.easybooking.reservation.presentation;

import com.example.easybooking.auth.annotation.RequireAuthenticatedUser;
import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.reservation.dto.calendar.OwnerCalendarResponse;
import com.example.easybooking.reservation.service.OwnerCalendarService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner/shops/{shopId}/calendar")
public class OwnerCalendarController {

    private final OwnerCalendarService ownerCalendarService;

    public OwnerCalendarController(OwnerCalendarService ownerCalendarService) {
        this.ownerCalendarService = ownerCalendarService;
    }

    @GetMapping
    public ResponseEntity<OwnerCalendarResponse> getCalendar(
            @PathVariable Long shopId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long staffId,
            @RequireAuthenticatedUser AuthenticatedUser user
    ) {
        return ResponseEntity.ok(ownerCalendarService.getCalendar(shopId, user.getUserId(), date, staffId));
    }
}
