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
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final StaffRepository staffRepository;
    private final ShopSettingsRepository shopSettingsRepository;
    private final HolidayChecker holidayChecker;
    private final OperatingTimeResolver operatingTimeResolver;
    private final SlotGenerator slotGenerator;
    private final ReservationTimeBlockRepository reservationTimeBlockRepository;
    private final Clock clock;

    public AvailabilityService(
            StaffRepository staffRepository,
            ShopSettingsRepository shopSettingsRepository,
            HolidayChecker holidayChecker,
            OperatingTimeResolver operatingTimeResolver,
            SlotGenerator slotGenerator,
            ReservationTimeBlockRepository reservationTimeBlockRepository
    ) {
        this(
                staffRepository,
                shopSettingsRepository,
                holidayChecker,
                operatingTimeResolver,
                slotGenerator,
                reservationTimeBlockRepository,
                Clock.system(SERVICE_ZONE_ID)
        );
    }

    public AvailabilityService(
            StaffRepository staffRepository,
            ShopSettingsRepository shopSettingsRepository,
            HolidayChecker holidayChecker,
            OperatingTimeResolver operatingTimeResolver,
            SlotGenerator slotGenerator,
            ReservationTimeBlockRepository reservationTimeBlockRepository,
            Clock clock
    ) {
        this.staffRepository = staffRepository;
        this.shopSettingsRepository = shopSettingsRepository;
        this.holidayChecker = holidayChecker;
        this.operatingTimeResolver = operatingTimeResolver;
        this.slotGenerator = slotGenerator;
        this.reservationTimeBlockRepository = reservationTimeBlockRepository;
        this.clock = clock;
    }

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
        return excludeOccupiedSlots(staffId, date, generatedSlots, shopSettings.getIntervalMinutes());
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
}
