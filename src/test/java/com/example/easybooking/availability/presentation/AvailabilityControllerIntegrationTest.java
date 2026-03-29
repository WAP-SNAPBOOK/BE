package com.example.easybooking.availability.presentation;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.availability.domain.PublicHoliday;
import com.example.easybooking.availability.domain.ShopHoliday;
import com.example.easybooking.availability.domain.ShopOperatingTime;
import com.example.easybooking.availability.domain.ShopSettings;
import com.example.easybooking.availability.domain.StaffOperatingTime;
import com.example.easybooking.availability.repository.PublicHolidayRepository;
import com.example.easybooking.availability.repository.ShopHolidayRepository;
import com.example.easybooking.availability.repository.ShopOperatingTimeRepository;
import com.example.easybooking.availability.repository.ShopSettingsRepository;
import com.example.easybooking.availability.repository.StaffOperatingTimeRepository;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.domain.ReservationTimeBlock;
import com.example.easybooking.reservation.domain.repository.ReservationRepository;
import com.example.easybooking.reservation.domain.repository.ReservationTimeBlockRepository;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.dto.request.CreateShopRequest;
import com.example.easybooking.shop.repository.ShopRepository;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.repository.StaffRepository;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.example.easybooking.user.domain.repository.UserRepository;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AvailabilityControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ShopRepository shopRepository;

    @Autowired
    StaffRepository staffRepository;

    @Autowired
    ShopSettingsRepository shopSettingsRepository;

    @Autowired
    ShopOperatingTimeRepository shopOperatingTimeRepository;

    @Autowired
    ShopHolidayRepository shopHolidayRepository;

    @Autowired
    StaffOperatingTimeRepository staffOperatingTimeRepository;

    @Autowired
    PublicHolidayRepository publicHolidayRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationTimeBlockRepository reservationTimeBlockRepository;

    @MockBean
    Clock clock;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAvailability_returnsAvailableSlots_whenRequestIsValid() throws Exception {
        freezeAt(LocalDateTime.of(2026, 2, 12, 9, 0));
        Fixture fixture = createFixture("201", "일별조회샵");
        shopOperatingTimeRepository.saveAndFlush(
                ShopOperatingTime.create(fixture.shopId(), DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(13, 0))
        );

        mockMvc.perform(get("/api/v1/shops/{shopId}/staff/{staffId}/availability", fixture.shopId(), fixture.staffId())
                        .param("date", "2026-02-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-02-16"))
                .andExpect(jsonPath("$.intervalMinutes").value(30))
                .andExpect(jsonPath("$.holiday").value(false))
                .andExpect(jsonPath("$.slots[0].time").value("10:00"))
                .andExpect(jsonPath("$.slots[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.slots[1].time").value("10:30"))
                .andExpect(jsonPath("$.slots[1].status").value("AVAILABLE"));
    }

    @Test
    void getAvailability_returnsNotFound_whenStaffDoesNotExist() throws Exception {
        freezeAt(LocalDateTime.of(2026, 2, 12, 9, 0));
        Fixture fixture = createFixture("202", "직원없는조회샵");

        mockMvc.perform(get("/api/v1/shops/{shopId}/staff/{staffId}/availability", fixture.shopId(), 999L)
                        .param("date", "2026-02-16"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("STAFF_NOT_FOUND"));
    }

    @Test
    void getAvailability_returnsHolidayTrueAndEmptySlots_whenDateIsHoliday() throws Exception {
        freezeAt(LocalDateTime.of(2026, 2, 12, 9, 0));
        Fixture fixture = createFixture("203", "휴무조회샵");
        shopOperatingTimeRepository.saveAndFlush(
                ShopOperatingTime.create(fixture.shopId(), DayOfWeek.SUNDAY, LocalTime.of(10, 0), LocalTime.of(13, 0))
        );
        shopHolidayRepository.saveAndFlush(ShopHoliday.createWeekly(fixture.shopId(), DayOfWeek.SUNDAY));

        mockMvc.perform(get("/api/v1/shops/{shopId}/staff/{staffId}/availability", fixture.shopId(), fixture.staffId())
                        .param("date", "2026-02-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-02-15"))
                .andExpect(jsonPath("$.holiday").value(true))
                .andExpect(jsonPath("$.slots.length()").value(0));
    }

    @Test
    void getAvailability_returnsBadRequest_whenDateExceedsBookingWindow() throws Exception {
        freezeAt(LocalDateTime.of(2026, 2, 12, 9, 0));
        Fixture fixture = createFixture("204", "예약기간초과샵");

        mockMvc.perform(get("/api/v1/shops/{shopId}/staff/{staffId}/availability", fixture.shopId(), fixture.staffId())
                        .param("date", "2026-03-15"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BOOKING_WINDOW_EXCEEDED"));
    }

    @Test
    void getMonthlyAvailability_returnsAvailableDates_whenRequestIsValid() throws Exception {
        freezeAt(LocalDateTime.of(2026, 2, 12, 9, 0));
        Fixture fixture = createFixture("205", "월조회샵");
        shopOperatingTimeRepository.saveAndFlush(
                ShopOperatingTime.create(fixture.shopId(), DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(13, 0))
        );

        mockMvc.perform(get("/api/v1/shops/{shopId}/staff/{staffId}/availability/monthly", fixture.shopId(), fixture.staffId())
                        .param("yearMonth", "2026-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearMonth").value("2026-03"))
                .andExpect(jsonPath("$.availableDates", hasItem(2)));
    }

    @Test
    void getMonthlyAvailability_returnsNotFound_whenStaffDoesNotExist() throws Exception {
        freezeAt(LocalDateTime.of(2026, 2, 12, 9, 0));
        Fixture fixture = createFixture("206", "월직원없는샵");

        mockMvc.perform(get("/api/v1/shops/{shopId}/staff/{staffId}/availability/monthly", fixture.shopId(), 999L)
                        .param("yearMonth", "2026-03"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("STAFF_NOT_FOUND"));
    }

    @Test
    void getMonthlyAvailability_excludesDatesOutsideBookingWindow() throws Exception {
        freezeAt(LocalDateTime.of(2026, 2, 12, 9, 0));
        Fixture fixture = createFixture("207", "월경계샵");
        shopOperatingTimeRepository.saveAndFlush(
                ShopOperatingTime.create(fixture.shopId(), DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(13, 0))
        );

        mockMvc.perform(get("/api/v1/shops/{shopId}/staff/{staffId}/availability/monthly", fixture.shopId(), fixture.staffId())
                        .param("yearMonth", "2026-04"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableDates.length()").value(0))
                .andExpect(jsonPath("$.holidayDates.length()").value(0))
                .andExpect(jsonPath("$.closedDates.length()").value(0));
    }

    @Test
    void getMonthlyAvailability_returnsAvailableHolidayClosedDatesBySpec() throws Exception {
        freezeAt(LocalDateTime.of(2026, 3, 1, 0, 0));
        Fixture fixture = createFixture("208", "월스펙검증샵");
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            shopOperatingTimeRepository.save(
                    ShopOperatingTime.create(fixture.shopId(), dayOfWeek, LocalTime.of(10, 0), LocalTime.of(13, 0))
            );
        }
        shopOperatingTimeRepository.flush();
        staffOperatingTimeRepository.saveAndFlush(StaffOperatingTime.createOff(fixture.staffId(), DayOfWeek.TUESDAY));
        shopHolidayRepository.saveAndFlush(ShopHoliday.createWeekly(fixture.shopId(), DayOfWeek.SUNDAY));
        publicHolidayRepository.saveAndFlush(PublicHoliday.create(LocalDate.of(2026, 3, 3), "대체공휴일"));

        ShopSettings settings = shopSettingsRepository.findByShopId(fixture.shopId()).orElseThrow();
        ReflectionTestUtils.setField(settings, "publicHolidayOff", true);
        shopSettingsRepository.saveAndFlush(settings);

        mockMvc.perform(get("/api/v1/shops/{shopId}/staff/{staffId}/availability/monthly", fixture.shopId(), fixture.staffId())
                        .param("yearMonth", "2026-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableDates", hasItem(2)))
                .andExpect(jsonPath("$.holidayDates", hasItem(1)))
                .andExpect(jsonPath("$.holidayDates", hasItem(3)))
                .andExpect(jsonPath("$.closedDates", hasItem(10)));
    }

    @Test
    void fullFlow_returnsSlotsOnMondayAndEmptyOnSundayAfterScheduleSetup() throws Exception {
        freezeAt(LocalDateTime.of(2026, 2, 12, 9, 0));
        Fixture fixture = createFixture("209", "전체플로우샵");
        authenticate(fixture.ownerId());

        mockMvc.perform(put("/api/v1/shops/{shopId}/schedule/interval", fixture.shopId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "intervalMinutes": 30
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/shops/{shopId}/schedule/operating-times", fixture.shopId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scheduleType": "DAILY",
                                  "times": [
                                    {"start": "10:00", "end": "13:00"},
                                    {"start": "14:00", "end": "18:00"}
                                  ]
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/shops/{shopId}/schedule/holidays", fixture.shopId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "holidayType": "WEEKLY",
                                  "dayOfWeek": "SUNDAY"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/shops/{shopId}/staff/{staffId}/availability", fixture.shopId(), fixture.staffId())
                        .param("date", "2026-02-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intervalMinutes").value(30))
                .andExpect(jsonPath("$.holiday").value(false))
                .andExpect(jsonPath("$.slots[*].time", hasItem("10:00")))
                .andExpect(jsonPath("$.slots[*].time", hasItem("10:30")))
                .andExpect(jsonPath("$.slots[*].time", hasItem("13:00")))
                .andExpect(jsonPath("$.slots[*].time", hasItem("14:00")))
                .andExpect(jsonPath("$.slots[*].time", hasItem("18:00")))
                .andExpect(jsonPath("$.slots[*].status", org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("AVAILABLE"))));

        mockMvc.perform(get("/api/v1/shops/{shopId}/staff/{staffId}/availability", fixture.shopId(), fixture.staffId())
                        .param("date", "2026-02-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holiday").value(true))
                .andExpect(jsonPath("$.slots.length()").value(0));
    }

    @Test
    void availabilityExcludesOccupiedTimeBlocks_afterReservationConfirmed() throws Exception {
        freezeAt(LocalDateTime.of(2026, 2, 12, 9, 0));
        Fixture fixture = createFixture("210", "예약블록반영샵");
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            shopOperatingTimeRepository.save(
                    ShopOperatingTime.create(fixture.shopId(), dayOfWeek, LocalTime.of(10, 0), LocalTime.of(13, 0))
            );
        }
        shopOperatingTimeRepository.flush();

        Reservation reservation = Reservation.createReservation(
                fixture.shopId(),
                fixture.ownerId(),
                fixture.ownerId() + 1000,
                LocalDate.of(2026, 2, 16),
                LocalTime.of(10, 0), List.of()
        );
        reservation.setStaffId(fixture.staffId());
        reservation.confirm("확정", 60);
        Reservation savedReservation = reservationRepository.saveAndFlush(reservation);
        reservationTimeBlockRepository.saveAllAndFlush(List.of(
                ReservationTimeBlock.create(savedReservation.getId(), fixture.staffId(), LocalDateTime.of(2026, 2, 16, 10, 0)),
                ReservationTimeBlock.create(savedReservation.getId(), fixture.staffId(), LocalDateTime.of(2026, 2, 16, 10, 30))
        ));

        mockMvc.perform(get("/api/v1/shops/{shopId}/staff/{staffId}/availability", fixture.shopId(), fixture.staffId())
                        .param("date", "2026-02-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slots[0].time").value("10:00"))
                .andExpect(jsonPath("$.slots[0].status").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.slots[1].time").value("10:30"))
                .andExpect(jsonPath("$.slots[1].status").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.slots[2].time").value("11:00"))
                .andExpect(jsonPath("$.slots[2].status").value("AVAILABLE"));
    }

    private Fixture createFixture(String suffix, String shopName) {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-avail-" + suffix, "owner-avail-" + suffix, "0109000" + suffix, UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName(shopName)
                        .address("서울")
                        .businessNumber("900-00-" + suffix)
                        .build()
        ));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(shop.getId()));
        Staff staff = staffRepository.saveAndFlush(Staff.create(shop.getId(), "직원-" + suffix));
        return new Fixture(owner.getId(), shop.getId(), staff.getId());
    }

    private void freezeAt(LocalDateTime localDateTime) {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        given(clock.getZone()).willReturn(zoneId);
        given(clock.instant()).willReturn(localDateTime.atZone(zoneId).toInstant());
    }

    private void authenticate(Long userId) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(userId, "OWNER"),
                null,
                null
        ));
        SecurityContextHolder.setContext(context);
    }

    private record Fixture(Long ownerId, Long shopId, Long staffId) {
    }
}

