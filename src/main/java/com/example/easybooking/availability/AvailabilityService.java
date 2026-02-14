package com.example.easybooking.availability;

import com.example.easybooking.availability.domain.ShopOperatingTime;
import com.example.easybooking.availability.domain.ShopSettings;
import com.example.easybooking.availability.exception.BookingWindowExceededException;
import com.example.easybooking.availability.exception.ShopSettingsNotFoundException;
import com.example.easybooking.availability.repository.ShopSettingsRepository;
import com.example.easybooking.reservation.domain.ReservationTimeBlock;
import com.example.easybooking.reservation.domain.repository.ReservationTimeBlockRepository;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.exception.StaffIdNotFoundException;
import com.example.easybooking.staff.repository.StaffRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final StaffRepository staffRepository;
    private final ShopSettingsRepository shopSettingsRepository;
    private final HolidayChecker holidayChecker;
    private final OperatingTimeResolver operatingTimeResolver;
    private final SlotGenerator slotGenerator;
    private final ReservationTimeBlockRepository reservationTimeBlockRepository;
    private final Clock clock;

    public List<LocalTime> getAvailableSlots(Long staffId, LocalDate date) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(StaffIdNotFoundException::new);

        LocalDate today = LocalDate.now(clock);
        ShopSettings shopSettings = shopSettingsRepository.findByShopId(staff.getShopId())
                .orElseThrow(ShopSettingsNotFoundException::new);
        validateDateWithinBookingWindow(date, today, shopSettings.getBookingWindowDays());

        if (holidayChecker.isHoliday(staff.getShopId(), date)) {
            return List.of();
        }

        List<ShopOperatingTime> operatingTimes = operatingTimeResolver.resolve(staffId, date.getDayOfWeek());
        List<LocalTime> generatedSlots = slotGenerator.generate(operatingTimes, shopSettings.getIntervalMinutes());
        List<LocalTime> leadFilteredSlots = applyMinBookingLeadForSameDay(
                generatedSlots,
                date,
                today,
                LocalDateTime.now(clock),
                shopSettings.getMinBookingLeadMinutes()
        );
        return excludeOccupiedSlots(staffId, date, leadFilteredSlots, shopSettings.getIntervalMinutes());
    }

    public List<Integer> getAvailableDatesInMonth(Long staffId, YearMonth yearMonth) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(StaffIdNotFoundException::new);
        ShopSettings shopSettings = shopSettingsRepository.findByShopId(staff.getShopId())
                .orElseThrow(ShopSettingsNotFoundException::new);

        LocalDate today = LocalDate.now(clock);
        LocalDate maxBookableDate = today.plusDays(shopSettings.getBookingWindowDays());
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        LocalDate startDate = monthStart.isAfter(today) ? monthStart : today;
        LocalDate endDate = monthEnd.isBefore(maxBookableDate) ? monthEnd : maxBookableDate;
        if (startDate.isAfter(endDate)) {
            return List.of();
        }

        List<Integer> availableDates = new ArrayList<>();
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            if (!getAvailableSlots(staffId, date).isEmpty()) {
                availableDates.add(date.getDayOfMonth());
            }
            date = date.plusDays(1);
        }

        return List.copyOf(availableDates);
    }

    private void validateDateWithinBookingWindow(LocalDate date, LocalDate today, int bookingWindowDays) {
        LocalDate maxBookableDate = today.plusDays(bookingWindowDays);
        if (date.isBefore(today) || date.isAfter(maxBookableDate)) {
            throw new BookingWindowExceededException();
        }
    }

    private List<LocalTime> excludeOccupiedSlots(
            Long staffId,
            LocalDate date,
            List<LocalTime> slots,
            int intervalMinutes
    ) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime nextDayStart = date.plusDays(1).atStartOfDay();
        List<LocalTime> occupiedTimes = reservationTimeBlockRepository
                .findByStaffIdAndBlockStartAtGreaterThanEqualAndBlockStartAtLessThan(staffId, dayStart, nextDayStart)
                .stream()
                .map(ReservationTimeBlock::getBlockStartAt)
                .map(LocalDateTime::toLocalTime)
                .toList();

        return slots.stream()
                .filter(slot -> occupiedTimes.stream()
                        .noneMatch(occupiedTime -> !occupiedTime.isBefore(slot)
                                && occupiedTime.isBefore(slot.plusMinutes(intervalMinutes))))
                .toList();
    }

    private List<LocalTime> applyMinBookingLeadForSameDay(
            List<LocalTime> slots,
            LocalDate targetDate,
            LocalDate today,
            LocalDateTime now,
            int minBookingLeadMinutes
    ) {
        if (!targetDate.isEqual(today)) {
            return slots;
        }
        LocalTime availableFrom = now.plusMinutes(minBookingLeadMinutes).toLocalTime();
        return slots.stream()
                .filter(slot -> !slot.isBefore(availableFrom))
                .toList();
    }
}
