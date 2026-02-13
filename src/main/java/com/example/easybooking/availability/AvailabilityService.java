package com.example.easybooking.availability;

import com.example.easybooking.availability.domain.ShopOperatingTime;
import com.example.easybooking.availability.domain.ShopSettings;
import com.example.easybooking.availability.exception.ShopSettingsNotFoundException;
import com.example.easybooking.availability.repository.ShopSettingsRepository;
import com.example.easybooking.reservation.domain.ReservationTimeBlock;
import com.example.easybooking.reservation.domain.repository.ReservationTimeBlockRepository;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.exception.StaffIdNotFoundException;
import com.example.easybooking.staff.repository.StaffRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public List<LocalTime> getAvailableSlots(Long staffId, LocalDate date) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(StaffIdNotFoundException::new);

        if (holidayChecker.isHoliday(staff.getShopId(), date)) {
            return List.of();
        }

        ShopSettings shopSettings = shopSettingsRepository.findByShopId(staff.getShopId())
                .orElseThrow(ShopSettingsNotFoundException::new);

        List<ShopOperatingTime> operatingTimes = operatingTimeResolver.resolve(staffId, date.getDayOfWeek());
        List<LocalTime> generatedSlots = slotGenerator.generate(operatingTimes, shopSettings.getIntervalMinutes());
        return excludeOccupiedSlots(staffId, date, generatedSlots, shopSettings.getIntervalMinutes());
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
}
