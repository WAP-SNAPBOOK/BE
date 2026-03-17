package com.example.easybooking.availability;

import com.example.easybooking.availability.domain.ShopSettings;
import com.example.easybooking.availability.dto.response.AvailabilityMonthlyResponse;
import com.example.easybooking.availability.dto.response.AvailabilitySlotsResponse;
import com.example.easybooking.availability.exception.ShopSettingsNotFoundException;
import com.example.easybooking.availability.repository.ShopSettingsRepository;
import com.example.easybooking.availability.result.DailyAvailabilityResult;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.exception.StaffIdNotFoundException;
import com.example.easybooking.staff.repository.StaffRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerAvailabilityService {

    private final AvailabilityService availabilityService;
    private final HolidayChecker holidayChecker;
    private final OperatingTimeResolver operatingTimeResolver;
    private final StaffRepository staffRepository;
    private final ShopSettingsRepository shopSettingsRepository;
    private final Clock clock;

    public AvailabilitySlotsResponse getDailyAvailability(Long shopId, Long staffId, LocalDate date) {
        validateStaffBelongsToShop(shopId, staffId);
        DailyAvailabilityResult result = availabilityService.getDailyAvailabilityResult(staffId, date);
        return AvailabilitySlotsResponse.of(result);
    }

    public AvailabilityMonthlyResponse getMonthlyAvailability(Long shopId, Long staffId, YearMonth yearMonth) {
        Staff staff = validateStaffBelongsToShop(shopId, staffId);
        ShopSettings settings = shopSettingsRepository.findByShopId(shopId)
                .orElseThrow(ShopSettingsNotFoundException::new);

        LocalDate today = LocalDate.now(clock);
        LocalDate maxBookableDate = today.plusDays(settings.getBookingWindowDays());
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        LocalDate startDate = monthStart.isAfter(today) ? monthStart : today;
        LocalDate endDate = monthEnd.isBefore(maxBookableDate) ? monthEnd : maxBookableDate;
        if (startDate.isAfter(endDate)) {
            return AvailabilityMonthlyResponse.of(yearMonth, List.of(), List.of(), List.of());
        }

        List<Integer> availableDates = new ArrayList<>();
        List<Integer> holidayDates = new ArrayList<>();
        List<Integer> closedDates = new ArrayList<>();

        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            if (holidayChecker.isHoliday(shopId, cursor)) {
                holidayDates.add(cursor.getDayOfMonth());
                cursor = cursor.plusDays(1);
                continue;
            }

            if (operatingTimeResolver.resolve(staff.getId(), cursor.getDayOfWeek()).isEmpty()) {
                closedDates.add(cursor.getDayOfMonth());
                cursor = cursor.plusDays(1);
                continue;
            }

            if (!availabilityService.getAvailableSlots(staff.getId(), cursor).isEmpty()) {
                availableDates.add(cursor.getDayOfMonth());
            }
            cursor = cursor.plusDays(1);
        }

        return AvailabilityMonthlyResponse.of(
                yearMonth,
                List.copyOf(availableDates),
                List.copyOf(holidayDates),
                List.copyOf(closedDates)
        );
    }

    private Staff validateStaffBelongsToShop(Long shopId, Long staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(StaffIdNotFoundException::new);
        if (!staff.getShopId().equals(shopId)) {
            throw new StaffIdNotFoundException();
        }
        return staff;
    }
}
