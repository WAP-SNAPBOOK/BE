package com.example.easybooking.availability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.easybooking.availability.domain.ShopOperatingTime;
import com.example.easybooking.availability.domain.ShopHoliday;
import com.example.easybooking.availability.domain.ShopSettings;
import com.example.easybooking.availability.domain.StaffOperatingTime;
import com.example.easybooking.availability.exception.BookingWindowExceededException;
import com.example.easybooking.availability.repository.PublicHolidayRepository;
import com.example.easybooking.availability.repository.ShopHolidayRepository;
import com.example.easybooking.availability.repository.ShopOperatingTimeRepository;
import com.example.easybooking.availability.repository.ShopSettingsRepository;
import com.example.easybooking.availability.repository.StaffOperatingTimeRepository;
import com.example.easybooking.reservation.domain.ReservationTimeBlock;
import com.example.easybooking.reservation.domain.repository.ReservationTimeBlockRepository;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.repository.StaffRepository;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class AvailabilityServiceTest {

    @Autowired
    StaffRepository staffRepository;

    @Autowired
    ShopSettingsRepository shopSettingsRepository;

    @Autowired
    ShopOperatingTimeRepository shopOperatingTimeRepository;

    @Autowired
    StaffOperatingTimeRepository staffOperatingTimeRepository;

    @Autowired
    ShopHolidayRepository shopHolidayRepository;

    @Autowired
    PublicHolidayRepository publicHolidayRepository;

    @Autowired
    ReservationTimeBlockRepository reservationTimeBlockRepository;

    @Test
    void getAvailableSlots_returnsOperatingTimeSlots_whenNoOverrideAndNoOccupiedBlocks() {
        AvailabilityService service = createService();
        Staff staff = staffRepository.saveAndFlush(Staff.create(1L, "직원A"));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(1L));
        shopOperatingTimeRepository.saveAndFlush(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(13, 0))
        );

        List<LocalTime> slots = service.getAvailableSlots(staff.getId(), LocalDate.of(2026, 2, 16));

        assertThat(slots).containsExactly(
                LocalTime.of(10, 0),
                LocalTime.of(10, 30),
                LocalTime.of(11, 0),
                LocalTime.of(11, 30),
                LocalTime.of(12, 0),
                LocalTime.of(12, 30),
                LocalTime.of(13, 0)
        );
    }

    @Test
    void getAvailableSlots_excludesOccupiedSlot_whenReservationTimeBlocksExist() {
        AvailabilityService service = createService();
        Staff staff = staffRepository.saveAndFlush(Staff.create(1L, "직원A"));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(1L));
        shopOperatingTimeRepository.saveAndFlush(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(13, 0))
        );
        reservationTimeBlockRepository.saveAllAndFlush(List.of(
                ReservationTimeBlock.create(1L, staff.getId(), LocalDate.of(2026, 2, 16).atTime(10, 30)),
                ReservationTimeBlock.create(1L, staff.getId(), LocalDate.of(2026, 2, 16).atTime(10, 40)),
                ReservationTimeBlock.create(1L, staff.getId(), LocalDate.of(2026, 2, 16).atTime(10, 50))
        ));

        List<LocalTime> slots = service.getAvailableSlots(staff.getId(), LocalDate.of(2026, 2, 16));

        assertThat(slots).containsExactly(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(11, 30),
                LocalTime.of(12, 0),
                LocalTime.of(12, 30),
                LocalTime.of(13, 0)
        );
    }

    @Test
    void getAvailableSlots_returnsEmpty_whenDateIsHoliday() {
        AvailabilityService service = createService();
        Staff staff = staffRepository.saveAndFlush(Staff.create(1L, "직원A"));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(1L));
        shopOperatingTimeRepository.saveAndFlush(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(13, 0))
        );
        shopHolidayRepository.saveAndFlush(ShopHoliday.createWeekly(1L, DayOfWeek.MONDAY));

        List<LocalTime> slots = service.getAvailableSlots(staff.getId(), LocalDate.of(2026, 2, 16));

        assertThat(slots).isEmpty();
    }

    @Test
    void getAvailableSlots_throwsException_whenDateExceedsBookingWindow() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-02-12T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        AvailabilityService service = createService(fixedClock);
        Staff staff = staffRepository.saveAndFlush(Staff.create(1L, "직원A"));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(1L));
        shopOperatingTimeRepository.saveAndFlush(
                ShopOperatingTime.create(1L, DayOfWeek.WEDNESDAY, LocalTime.of(10, 0), LocalTime.of(13, 0))
        );

        assertThatThrownBy(() -> service.getAvailableSlots(staff.getId(), LocalDate.of(2026, 4, 1)))
                .isInstanceOf(BookingWindowExceededException.class);
    }

    @Test
    void getAvailableSlots_appliesMinBookingLead_forSameDay() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-02-16T01:30:00Z"), ZoneId.of("Asia/Seoul"));
        AvailabilityService service = createService(fixedClock);
        Staff staff = staffRepository.saveAndFlush(Staff.create(1L, "직원A"));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(1L));
        shopOperatingTimeRepository.saveAndFlush(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(13, 0))
        );

        List<LocalTime> slots = service.getAvailableSlots(staff.getId(), LocalDate.of(2026, 2, 16));

        assertThat(slots).containsExactly(
                LocalTime.of(11, 30),
                LocalTime.of(12, 0),
                LocalTime.of(12, 30),
                LocalTime.of(13, 0)
        );
    }

    @Test
    void getAvailableSlots_returnsStaffOverrideSlots_whenOverrideExists() {
        AvailabilityService service = createService();
        Staff staff = staffRepository.saveAndFlush(Staff.create(1L, "직원A"));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(1L));
        shopOperatingTimeRepository.saveAndFlush(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(19, 0))
        );
        staffOperatingTimeRepository.saveAndFlush(
                StaffOperatingTime.create(staff.getId(), DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(15, 0))
        );

        List<LocalTime> slots = service.getAvailableSlots(staff.getId(), LocalDate.of(2026, 2, 16));

        assertThat(slots).containsExactly(
                LocalTime.of(10, 0),
                LocalTime.of(10, 30),
                LocalTime.of(11, 0),
                LocalTime.of(11, 30),
                LocalTime.of(12, 0),
                LocalTime.of(12, 30),
                LocalTime.of(13, 0),
                LocalTime.of(13, 30),
                LocalTime.of(14, 0),
                LocalTime.of(14, 30),
                LocalTime.of(15, 0)
        );
    }

    @Test
    void getAvailableSlots_returnsEmpty_whenStaffIsOff() {
        AvailabilityService service = createService();
        Staff staff = staffRepository.saveAndFlush(Staff.create(1L, "직원A"));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(1L));
        shopOperatingTimeRepository.saveAndFlush(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(19, 0))
        );
        staffOperatingTimeRepository.saveAndFlush(
                StaffOperatingTime.createOff(staff.getId(), DayOfWeek.MONDAY)
        );

        List<LocalTime> slots = service.getAvailableSlots(staff.getId(), LocalDate.of(2026, 2, 16));

        assertThat(slots).isEmpty();
    }

    private AvailabilityService createService() {
        return new AvailabilityService(
                staffRepository,
                shopSettingsRepository,
                new HolidayChecker(shopHolidayRepository, publicHolidayRepository, shopSettingsRepository),
                new OperatingTimeResolver(staffRepository, shopOperatingTimeRepository, staffOperatingTimeRepository),
                new SlotGenerator(),
                reservationTimeBlockRepository
        );
    }

    private AvailabilityService createService(Clock clock) {
        return new AvailabilityService(
                staffRepository,
                shopSettingsRepository,
                new HolidayChecker(shopHolidayRepository, publicHolidayRepository, shopSettingsRepository),
                new OperatingTimeResolver(staffRepository, shopOperatingTimeRepository, staffOperatingTimeRepository),
                new SlotGenerator(),
                reservationTimeBlockRepository,
                clock
        );
    }
}
