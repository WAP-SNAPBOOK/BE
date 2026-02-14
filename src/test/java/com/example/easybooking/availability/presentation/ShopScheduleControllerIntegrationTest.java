package com.example.easybooking.availability.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.availability.domain.ShopHoliday;
import com.example.easybooking.availability.domain.ShopOperatingTime;
import com.example.easybooking.availability.domain.ShopSettings;
import com.example.easybooking.availability.repository.ShopHolidayRepository;
import com.example.easybooking.availability.repository.ShopOperatingTimeRepository;
import com.example.easybooking.availability.repository.ShopSettingsRepository;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.dto.request.CreateShopRequest;
import com.example.easybooking.shop.repository.ShopRepository;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.example.easybooking.user.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ShopScheduleControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ShopRepository shopRepository;

    @Autowired
    ShopSettingsRepository shopSettingsRepository;

    @Autowired
    ShopOperatingTimeRepository shopOperatingTimeRepository;

    @Autowired
    ShopHolidayRepository shopHolidayRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getScheduleSettings_returnsShopSettings_whenOwnerAuthenticated() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-101", "owner", "01012341234", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("테스트샵")
                        .address("서울")
                        .businessNumber("123-45-67890")
                        .build()
        ));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(shop.getId()));
        authenticate(owner.getId());

        mockMvc.perform(get("/api/v1/shops/{shopId}/schedule/settings", shop.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shopId").value(shop.getId()))
                .andExpect(jsonPath("$.intervalMinutes").value(30))
                .andExpect(jsonPath("$.scheduleType").value("DAILY"))
                .andExpect(jsonPath("$.bookingWindowDays").value(30))
                .andExpect(jsonPath("$.minBookingLeadMinutes").value(60))
                .andExpect(jsonPath("$.publicHolidayOff").value(false));
    }

    @Test
    void updateScheduleSettings_updatesIntervalMinutes_whenOwnerAuthenticated() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-102", "owner2", "01022223333", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("테스트샵2")
                        .address("서울")
                        .businessNumber("111-22-33333")
                        .build()
        ));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(shop.getId()));
        authenticate(owner.getId());

        mockMvc.perform(put("/api/v1/shops/{shopId}/schedule/settings", shop.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "intervalMinutes": 60,
                                  "bookingWindowDays": 30,
                                  "minBookingLeadMinutes": 60,
                                  "publicHolidayOff": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shopId").value(shop.getId()))
                .andExpect(jsonPath("$.intervalMinutes").value(60));
    }

    @Test
    void updateOperatingTimes_savesRowsForAllDays_whenScheduleTypeDaily() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-103", "owner3", "01033334444", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("테스트샵3")
                        .address("서울")
                        .businessNumber("222-33-44444")
                        .build()
        ));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(shop.getId()));
        authenticate(owner.getId());

        mockMvc.perform(put("/api/v1/shops/{shopId}/schedule/operating-times", shop.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scheduleType": "DAILY",
                                  "times": [
                                    {"start": "10:00", "end": "13:00"},
                                    {"start": "14:00", "end": "19:00"}
                                  ]
                                }
                                """))
                .andExpect(status().isOk());

        List<ShopOperatingTime> saved = shopOperatingTimeRepository.findByShopId(shop.getId());
        List<ShopOperatingTime> mondayRows = shopOperatingTimeRepository
                .findByShopIdAndDayOfWeek(shop.getId(), DayOfWeek.MONDAY);

        org.assertj.core.api.Assertions.assertThat(saved).hasSize(14);
        org.assertj.core.api.Assertions.assertThat(mondayRows).hasSize(2);
        org.assertj.core.api.Assertions.assertThat(mondayRows)
                .extracting(ShopOperatingTime::getStartTime, ShopOperatingTime::getEndTime)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(LocalTime.of(10, 0), LocalTime.of(13, 0)),
                        org.assertj.core.groups.Tuple.tuple(LocalTime.of(14, 0), LocalTime.of(19, 0))
                );
    }

    @Test
    void updateOperatingTimes_savesWeekdayWeekendRows_whenScheduleTypeWeekdayWeekend() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-104", "owner4", "01044445555", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("테스트샵4")
                        .address("서울")
                        .businessNumber("333-44-55555")
                        .build()
        ));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(shop.getId()));
        authenticate(owner.getId());

        mockMvc.perform(put("/api/v1/shops/{shopId}/schedule/operating-times", shop.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scheduleType": "WEEKDAY_WEEKEND",
                                  "weekdayTimes": [{"start": "10:00", "end": "19:00"}],
                                  "weekendTimes": [{"start": "11:00", "end": "17:00"}]
                                }
                                """))
                .andExpect(status().isOk());

        List<ShopOperatingTime> saved = shopOperatingTimeRepository.findByShopId(shop.getId());
        org.assertj.core.api.Assertions.assertThat(saved).hasSize(7);
        org.assertj.core.api.Assertions.assertThat(
                shopOperatingTimeRepository.findByShopIdAndDayOfWeek(shop.getId(), DayOfWeek.MONDAY))
                .hasSize(1)
                .extracting(ShopOperatingTime::getStartTime, ShopOperatingTime::getEndTime)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(LocalTime.of(10, 0), LocalTime.of(19, 0)));
        org.assertj.core.api.Assertions.assertThat(
                shopOperatingTimeRepository.findByShopIdAndDayOfWeek(shop.getId(), DayOfWeek.SATURDAY))
                .hasSize(1)
                .extracting(ShopOperatingTime::getStartTime, ShopOperatingTime::getEndTime)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(LocalTime.of(11, 0), LocalTime.of(17, 0)));
    }

    @Test
    void updateOperatingTimes_savesRowsByDay_whenScheduleTypeByDay() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-105", "owner5", "01055556666", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("테스트샵5")
                        .address("서울")
                        .businessNumber("444-55-66666")
                        .build()
        ));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(shop.getId()));
        authenticate(owner.getId());

        mockMvc.perform(put("/api/v1/shops/{shopId}/schedule/operating-times", shop.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scheduleType": "BY_DAY",
                                  "dayTimes": {
                                    "MONDAY": [{"start": "10:00", "end": "19:00"}],
                                    "TUESDAY": [{"start": "10:00", "end": "17:00"}]
                                  }
                                }
                                """))
                .andExpect(status().isOk());

        List<ShopOperatingTime> saved = shopOperatingTimeRepository.findByShopId(shop.getId());
        org.assertj.core.api.Assertions.assertThat(saved).hasSize(2);
        org.assertj.core.api.Assertions.assertThat(
                shopOperatingTimeRepository.findByShopIdAndDayOfWeek(shop.getId(), DayOfWeek.MONDAY))
                .hasSize(1)
                .extracting(ShopOperatingTime::getStartTime, ShopOperatingTime::getEndTime)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(LocalTime.of(10, 0), LocalTime.of(19, 0)));
        org.assertj.core.api.Assertions.assertThat(
                shopOperatingTimeRepository.findByShopIdAndDayOfWeek(shop.getId(), DayOfWeek.TUESDAY))
                .hasSize(1)
                .extracting(ShopOperatingTime::getStartTime, ShopOperatingTime::getEndTime)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(LocalTime.of(10, 0), LocalTime.of(17, 0)));
    }

    @Test
    void getOperatingTimes_returnsScheduleTypeAndDayTimes_whenOwnerAuthenticated() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-106", "owner6", "01066667777", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("테스트샵6")
                        .address("서울")
                        .businessNumber("555-66-77777")
                        .build()
        ));
        ShopSettings settings = ShopSettings.createDefault(shop.getId());
        settings.updateScheduleType(com.example.easybooking.availability.domain.ScheduleType.BY_DAY);
        shopSettingsRepository.saveAndFlush(settings);
        shopOperatingTimeRepository.saveAllAndFlush(List.of(
                ShopOperatingTime.create(shop.getId(), DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(19, 0)),
                ShopOperatingTime.create(shop.getId(), DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(17, 0))
        ));
        authenticate(owner.getId());

        mockMvc.perform(get("/api/v1/shops/{shopId}/schedule/operating-times", shop.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleType").value("BY_DAY"))
                .andExpect(jsonPath("$.dayTimes.MONDAY[0].start").value("10:00:00"))
                .andExpect(jsonPath("$.dayTimes.MONDAY[0].end").value("19:00:00"))
                .andExpect(jsonPath("$.dayTimes.TUESDAY[0].start").value("10:00:00"))
                .andExpect(jsonPath("$.dayTimes.TUESDAY[0].end").value("17:00:00"));
    }

    @Test
    void scheduleApis_returnForbidden_whenUserIsNotShopOwner() throws Exception {
        User realOwner = userRepository.saveAndFlush(
                User.createUser("kakao-107", "owner7", "01077778888", UserType.OWNER)
        );
        User otherOwner = userRepository.saveAndFlush(
                User.createUser("kakao-108", "owner8", "01088889999", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                realOwner.getId(),
                CreateShopRequest.builder()
                        .businessName("테스트샵7")
                        .address("서울")
                        .businessNumber("666-77-88888")
                        .build()
        ));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(shop.getId()));
        authenticate(otherOwner.getId());

        mockMvc.perform(get("/api/v1/shops/{shopId}/schedule/settings", shop.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SHOP_OWNER_MISMATCH"));
    }

    @Test
    void getHolidays_returnsAllConfiguredHolidays_whenOwnerAuthenticated() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-109", "owner9", "01099990000", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("휴무일샵")
                        .address("서울")
                        .businessNumber("777-88-99999")
                        .build()
        ));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(shop.getId()));
        shopHolidayRepository.saveAllAndFlush(List.of(
                ShopHoliday.createWeekly(shop.getId(), DayOfWeek.SUNDAY),
                ShopHoliday.createCustom(shop.getId(), java.time.LocalDate.of(2026, 3, 15)),
                ShopHoliday.createCustom(shop.getId(), java.time.LocalDate.of(2026, 3, 22))
        ));
        authenticate(owner.getId());

        mockMvc.perform(get("/api/v1/shops/{shopId}/schedule/holidays", shop.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holidays.length()").value(3));
    }

    @Test
    void createHoliday_savesWeeklyHoliday_whenTypeIsWeekly() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-110", "owner10", "01011112222", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("주간휴무샵")
                        .address("서울")
                        .businessNumber("888-99-00001")
                        .build()
        ));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(shop.getId()));
        authenticate(owner.getId());

        mockMvc.perform(post("/api/v1/shops/{shopId}/schedule/holidays", shop.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "holidayType": "WEEKLY",
                                  "dayOfWeek": "SUNDAY"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.holidayType").value("WEEKLY"))
                .andExpect(jsonPath("$.dayOfWeek").value("SUNDAY"));

        org.assertj.core.api.Assertions.assertThat(shopHolidayRepository.findByShopId(shop.getId()))
                .hasSize(1)
                .extracting(ShopHoliday::getHolidayType, ShopHoliday::getDayOfWeek)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(
                        com.example.easybooking.availability.domain.HolidayType.WEEKLY,
                        DayOfWeek.SUNDAY
                ));
    }

    @Test
    void createHoliday_savesBiweeklyHoliday_whenTypeIsBiweekly() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-111", "owner11", "01022224444", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("격주휴무샵")
                        .address("서울")
                        .businessNumber("888-99-00002")
                        .build()
        ));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(shop.getId()));
        authenticate(owner.getId());

        mockMvc.perform(post("/api/v1/shops/{shopId}/schedule/holidays", shop.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "holidayType": "BIWEEKLY",
                                  "dayOfWeek": "SATURDAY",
                                  "referenceDate": "2026-02-14"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.holidayType").value("BIWEEKLY"))
                .andExpect(jsonPath("$.dayOfWeek").value("SATURDAY"))
                .andExpect(jsonPath("$.referenceDate").value("2026-02-14"));

        org.assertj.core.api.Assertions.assertThat(shopHolidayRepository.findByShopId(shop.getId()))
                .hasSize(1)
                .extracting(ShopHoliday::getHolidayType, ShopHoliday::getDayOfWeek, ShopHoliday::getReferenceDate)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(
                        com.example.easybooking.availability.domain.HolidayType.BIWEEKLY,
                        DayOfWeek.SATURDAY,
                        java.time.LocalDate.of(2026, 2, 14)
                ));
    }

    @Test
    void createHoliday_savesMonthlyHoliday_whenTypeIsMonthly() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-112", "owner12", "01033335555", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("월간휴무샵")
                        .address("서울")
                        .businessNumber("888-99-00003")
                        .build()
        ));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(shop.getId()));
        authenticate(owner.getId());

        mockMvc.perform(post("/api/v1/shops/{shopId}/schedule/holidays", shop.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "holidayType": "MONTHLY",
                                  "weekOfMonth": 2,
                                  "dayOfWeek": "MONDAY"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.holidayType").value("MONTHLY"))
                .andExpect(jsonPath("$.weekOfMonth").value(2))
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"));

        org.assertj.core.api.Assertions.assertThat(shopHolidayRepository.findByShopId(shop.getId()))
                .hasSize(1)
                .extracting(ShopHoliday::getHolidayType, ShopHoliday::getWeekOfMonth, ShopHoliday::getDayOfWeek)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(
                        com.example.easybooking.availability.domain.HolidayType.MONTHLY,
                        2,
                        DayOfWeek.MONDAY
                ));
    }

    @Test
    void createHoliday_savesCustomHoliday_whenTypeIsCustom() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-113", "owner13", "01044446666", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("커스텀휴무샵")
                        .address("서울")
                        .businessNumber("888-99-00004")
                        .build()
        ));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(shop.getId()));
        authenticate(owner.getId());

        mockMvc.perform(post("/api/v1/shops/{shopId}/schedule/holidays", shop.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "holidayType": "CUSTOM",
                                  "specificDate": "2026-03-15"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.holidayType").value("CUSTOM"))
                .andExpect(jsonPath("$.specificDate").value("2026-03-15"));

        org.assertj.core.api.Assertions.assertThat(shopHolidayRepository.findByShopId(shop.getId()))
                .hasSize(1)
                .extracting(ShopHoliday::getHolidayType, ShopHoliday::getSpecificDate)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(
                        com.example.easybooking.availability.domain.HolidayType.CUSTOM,
                        java.time.LocalDate.of(2026, 3, 15)
                ));
    }

    @Test
    void deleteHoliday_removesConfiguredHoliday_whenHolidayExists() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-114", "owner14", "01055557777", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("휴무삭제샵")
                        .address("서울")
                        .businessNumber("888-99-00005")
                        .build()
        ));
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(shop.getId()));
        ShopHoliday holiday = shopHolidayRepository.saveAndFlush(
                ShopHoliday.createWeekly(shop.getId(), DayOfWeek.SUNDAY)
        );
        authenticate(owner.getId());

        mockMvc.perform(delete("/api/v1/shops/{shopId}/schedule/holidays/{holidayId}", shop.getId(), holiday.getId()))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(shopHolidayRepository.findByShopId(shop.getId())).isEmpty();
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
}
