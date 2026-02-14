package com.example.easybooking.availability.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.availability.domain.StaffOperatingTime;
import com.example.easybooking.availability.domain.ShopOperatingTime;
import com.example.easybooking.availability.repository.ShopOperatingTimeRepository;
import com.example.easybooking.availability.repository.StaffOperatingTimeRepository;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.dto.request.CreateShopRequest;
import com.example.easybooking.shop.repository.ShopRepository;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.repository.StaffRepository;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.example.easybooking.user.domain.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.LocalTime;
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

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class StaffOperatingTimesControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ShopRepository shopRepository;

    @Autowired
    StaffRepository staffRepository;

    @Autowired
    StaffOperatingTimeRepository staffOperatingTimeRepository;

    @Autowired
    ShopOperatingTimeRepository shopOperatingTimeRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void putStaffOperatingTimes_savesOverrides_whenOwnerAuthenticated() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-201", "owner", "01011112222", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("직원테스트샵")
                        .address("서울")
                        .businessNumber("999-99-99999")
                        .build()
        ));
        Staff staff = staffRepository.saveAndFlush(Staff.create(shop.getId(), "직원A"));
        shopOperatingTimeRepository.saveAndFlush(
                ShopOperatingTime.create(shop.getId(), DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(19, 0))
        );
        authenticate(owner.getId());

        mockMvc.perform(put("/api/v1/shops/{shopId}/staff/{staffId}/operating-times", shop.getId(), staff.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "overrides": [
                                    {"dayOfWeek": "MONDAY", "isOff": false, "start": "10:00", "end": "17:00"},
                                    {"dayOfWeek": "WEDNESDAY", "isOff": true}
                                  ]
                                }
                                """))
                .andExpect(status().isOk());

        StaffOperatingTime monday = staffOperatingTimeRepository
                .findByStaffIdAndDayOfWeek(staff.getId(), DayOfWeek.MONDAY)
                .orElseThrow();
        StaffOperatingTime wednesday = staffOperatingTimeRepository
                .findByStaffIdAndDayOfWeek(staff.getId(), DayOfWeek.WEDNESDAY)
                .orElseThrow();

        org.assertj.core.api.Assertions.assertThat(monday.isOff()).isFalse();
        org.assertj.core.api.Assertions.assertThat(monday.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        org.assertj.core.api.Assertions.assertThat(monday.getEndTime()).isEqualTo(LocalTime.of(17, 0));
        org.assertj.core.api.Assertions.assertThat(wednesday.isOff()).isTrue();
    }

    @Test
    void getStaffOperatingTimes_returnsOverrides_whenOwnerAuthenticated() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-202", "owner2", "01012121212", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("직원조회샵")
                        .address("서울")
                        .businessNumber("111-11-11111")
                        .build()
        ));
        Staff staff = staffRepository.saveAndFlush(Staff.create(shop.getId(), "직원B"));
        staffOperatingTimeRepository.saveAllAndFlush(java.util.List.of(
                StaffOperatingTime.create(staff.getId(), DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(17, 0)),
                StaffOperatingTime.createOff(staff.getId(), DayOfWeek.WEDNESDAY)
        ));
        authenticate(owner.getId());

        mockMvc.perform(get("/api/v1/shops/{shopId}/staff/{staffId}/operating-times", shop.getId(), staff.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overrides[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.overrides[0].isOff").value(false))
                .andExpect(jsonPath("$.overrides[0].start").value("10:00:00"))
                .andExpect(jsonPath("$.overrides[0].end").value("17:00:00"))
                .andExpect(jsonPath("$.overrides[1].dayOfWeek").value("WEDNESDAY"))
                .andExpect(jsonPath("$.overrides[1].isOff").value(true));
    }

    @Test
    void putStaffOperatingTimes_returnsBadRequest_whenOverrideExceedsShopOperatingRange() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-203", "owner3", "01023232323", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("직원검증샵")
                        .address("서울")
                        .businessNumber("222-22-22222")
                        .build()
        ));
        Staff staff = staffRepository.saveAndFlush(Staff.create(shop.getId(), "직원C"));
        shopOperatingTimeRepository.saveAndFlush(
                ShopOperatingTime.create(shop.getId(), DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(19, 0))
        );
        authenticate(owner.getId());

        mockMvc.perform(put("/api/v1/shops/{shopId}/staff/{staffId}/operating-times", shop.getId(), staff.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "overrides": [
                                    {"dayOfWeek": "MONDAY", "isOff": false, "start": "08:00", "end": "20:00"}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest());
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
