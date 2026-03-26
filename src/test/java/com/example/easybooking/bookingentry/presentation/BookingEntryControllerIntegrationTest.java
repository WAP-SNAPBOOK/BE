package com.example.easybooking.bookingentry.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.dto.request.CreateShopRequest;
import com.example.easybooking.shop.repository.ShopRepository;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.repository.StaffRepository;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.example.easybooking.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BookingEntryControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ShopRepository shopRepository;

    @Autowired
    StaffRepository staffRepository;

    @Test
    void getPublicBookingEntry_returnsOrderedStaffsAndDefaultStaff_whenSlugMatches() throws Exception {
        Fixture fixture = createFixture("301", "slug-booking-entry", "슬러그예약샵", true);
        Staff firstStaff = staffRepository.saveAndFlush(Staff.create(fixture.shopId(), "직원-B"));
        Staff secondStaff = staffRepository.saveAndFlush(Staff.create(fixture.shopId(), "직원-A"));

        mockMvc.perform(get("/api/public/shops/{slugOrCode}/booking-entry", fixture.slugOrCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shopId").value(fixture.shopId()))
                .andExpect(jsonPath("$.shopName").value("슬러그예약샵"))
                .andExpect(jsonPath("$.defaultStaffId").value(firstStaff.getId()))
                .andExpect(jsonPath("$.staffs[0].staffId").value(firstStaff.getId()))
                .andExpect(jsonPath("$.staffs[0].name").value("직원-B"))
                .andExpect(jsonPath("$.staffs[1].staffId").value(secondStaff.getId()))
                .andExpect(jsonPath("$.staffs[1].name").value("직원-A"));
    }

    @Test
    void getPublicBookingEntry_fallsBackToPublicCode_whenSlugDoesNotExist() throws Exception {
        Fixture fixture = createFixture("302", null, "코드예약샵", false);
        Staff staff = staffRepository.saveAndFlush(Staff.create(fixture.shopId(), "직원-코드"));

        mockMvc.perform(get("/api/public/shops/{slugOrCode}/booking-entry", fixture.slugOrCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shopId").value(fixture.shopId()))
                .andExpect(jsonPath("$.shopName").value("코드예약샵"))
                .andExpect(jsonPath("$.defaultStaffId").value(staff.getId()))
                .andExpect(jsonPath("$.staffs[0].staffId").value(staff.getId()))
                .andExpect(jsonPath("$.staffs[0].name").value("직원-코드"));
    }

    @Test
    void getBookingEntry_returnsShopAndStaffs_whenShopIdMatches() throws Exception {
        Fixture fixture = createFixture("303", "shop-id-booking-entry", "아이디예약샵", true);
        Staff staff = staffRepository.saveAndFlush(Staff.create(fixture.shopId(), "직원-아이디"));

        mockMvc.perform(get("/api/v1/shops/{shopId}/booking-entry", fixture.shopId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shopId").value(fixture.shopId()))
                .andExpect(jsonPath("$.shopName").value("아이디예약샵"))
                .andExpect(jsonPath("$.defaultStaffId").value(staff.getId()))
                .andExpect(jsonPath("$.staffs[0].staffId").value(staff.getId()))
                .andExpect(jsonPath("$.staffs[0].name").value("직원-아이디"));
    }

    private Fixture createFixture(String suffix, String slug, String shopName, boolean useSlug) {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-booking-entry-" + suffix, "owner-booking-entry-" + suffix, "0109300" + suffix, UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName(shopName)
                        .address("서울")
                        .businessNumber("930-00-" + suffix)
                        .build()
        ));
        String publicCode = "code-" + suffix;
        shop.assignPublicCode(publicCode);
        if (useSlug) {
            shop.updateSlug(slug);
        }
        Shop savedShop = shopRepository.saveAndFlush(shop);
        return new Fixture(savedShop.getId(), useSlug ? slug : publicCode);
    }

    private record Fixture(Long shopId, String slugOrCode) {
    }
}
