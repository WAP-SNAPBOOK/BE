package com.example.easybooking.bookingentry.presentation;

import com.example.easybooking.bookingentry.BookingEntryService;
import com.example.easybooking.bookingentry.dto.response.BookingEntryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookingEntryController {

    private final BookingEntryService bookingEntryService;

    @GetMapping("/api/public/shops/{slugOrCode}/booking-entry")
    public ResponseEntity<BookingEntryResponse> getPublicBookingEntry(@PathVariable String slugOrCode) {
        return ResponseEntity.ok(bookingEntryService.getBySlugOrCode(slugOrCode));
    }

    @GetMapping("/api/v1/shops/{shopId}/booking-entry")
    public ResponseEntity<BookingEntryResponse> getBookingEntry(@PathVariable Long shopId) {
        return ResponseEntity.ok(bookingEntryService.getByShopId(shopId));
    }
}
