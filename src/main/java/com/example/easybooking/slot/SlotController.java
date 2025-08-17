package com.example.easybooking.slot;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/slot/{shopId}")
public class SlotController {

    private final SlotService slotService;

    @GetMapping("/available-dates")
    public ResponseEntity<List<LocalDate>> getAvailableDates(
            @PathVariable Long shopId,
            @RequestParam int year,
            @RequestParam int month) {
        List<LocalDate> dates = slotService.getAvailableDates(shopId, year, month);
        return ResponseEntity.ok(dates);
    }

    @GetMapping("/available-times")
    public ResponseEntity<List<LocalTime>> getAvailableTimes(
            @PathVariable Long shopId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<LocalTime> times = slotService.getAvailableTimes(shopId, date);
        return ResponseEntity.ok(times);
    }

    @PostMapping("/slots")
    public ResponseEntity<Void> makeSlot(
            @RequestBody AddSlotDto addSlotDto,
            @PathVariable Long shopId) {
        slotService.makeSlot(addSlotDto, shopId);
        return ResponseEntity.ok().build();
    }

}