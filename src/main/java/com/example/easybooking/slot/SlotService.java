package com.example.easybooking.slot;

import com.example.easybooking.shop.Shop;
import com.example.easybooking.shop.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepository slotRepository;
    private final ShopRepository shopRepository;
    public List<LocalDate> getAvailableDates(Long shopId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        return slotRepository.findDistinctAvailableDates(shopId, startOfMonth, endOfMonth);
    }

    public List<LocalTime> getAvailableTimes(Long shopId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<Slot> slots = slotRepository
                .findByShopIdAndIsBookedFalseAndStartDateTimeBetween(shopId, startOfDay, endOfDay);

        return slots.stream()
                .map(slot -> slot.getStartDateTime().toLocalTime())
                .sorted()
                .collect(Collectors.toList());
    }

    public void makeSlot(AddSlotDto addSlotDto, Long shopId) {
        List<LocalDateTime> startDateTimes = addSlotDto.getStartDateTimes();
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found with id: " + shopId));
        for (LocalDateTime startDateTime : startDateTimes) {
            Slot slot = Slot.create(shop, startDateTime);
            slotRepository.save(slot);
        }
    }
}
