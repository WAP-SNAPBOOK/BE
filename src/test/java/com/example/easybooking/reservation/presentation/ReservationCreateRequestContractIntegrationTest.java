package com.example.easybooking.reservation.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.reservation.domain.repository.ReservationRepository;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.dto.request.CreateShopRequest;
import com.example.easybooking.shop.repository.ShopRepository;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.repository.StaffRepository;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.example.easybooking.user.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ReservationCreateRequestContractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ShopRepository shopRepository;

    @Autowired
    StaffRepository staffRepository;

    @Autowired
    ReservationRepository reservationRepository;

    private Long shopId;
    private Long staffId;

    @BeforeEach
    void setUp() {
        User owner = userRepository.save(User.createUser(
                "owner-" + UUID.randomUUID(),
                "owner",
                "010-1111-1111",
                UserType.OWNER
        ));
        User customer = userRepository.save(User.createUser(
                "customer-" + UUID.randomUUID(),
                "customer",
                "010-2222-2222",
                UserType.CUSTOMER
        ));

        Shop shop = shopRepository.save(Shop.create(owner.getId(), CreateShopRequest.builder()
                .businessName("shop")
                .address("seoul")
                .businessNumber("123")
                .build()));
        Staff staff = staffRepository.save(Staff.create(shop.getId(), "default-staff"));
        this.shopId = shop.getId();
        this.staffId = staff.getId();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(customer.getId(), "ROLE_USER"),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        ));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("레거시 formData 요청만으로는 예약 생성이 실패해야 한다")
    void createReservation_withFormDataOnly_returns4xx() throws Exception {
        Map<String, Object> formData = new HashMap<>();
        formData.put("date", "2026-03-15");
        formData.put("time", "10:00");
        formData.put("requests", "legacy");

        Map<String, Object> payload = new HashMap<>();
        payload.put("shopId", shopId);
        payload.put("staffId", staffId);
        payload.put("formData", formData);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("예약 생성 요청에서 date 누락 시 400을 반환한다")
    void createReservation_withoutDate_returns400() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("shopId", shopId);
        payload.put("staffId", staffId);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUIRED_DATE_MISSING"));
    }

    @Test
    @DisplayName("예약 생성 요청에서 time 누락 시 400을 반환한다")
    void createReservation_withoutTime_returns400() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("shopId", shopId);
        payload.put("staffId", staffId);
        payload.put("date", "2026-03-15");

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUIRED_TIME_MISSING"));
    }

    @Test
    @DisplayName("신규 필드(date/time/requirements/imageUrls)로 예약 생성이 성공한다")
    void createReservation_withNewFields_returns201() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("shopId", shopId);
        payload.put("staffId", staffId);
        payload.put("date", "2026-03-16");
        payload.put("time", "10:00");
        payload.put("requirements", "short nails");
        payload.put("imageUrls", List.of("https://example.com/a.jpg", "https://example.com/b.jpg"));
        payload.put("menuSelections", List.of());

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.date").value("2026-03-16"))
                .andExpect(jsonPath("$.time").value("10:00:00"))
                .andExpect(jsonPath("$.requests").value("short nails"))
                .andExpect(jsonPath("$.requirements").value("short nails"))
                .andExpect(jsonPath("$.photoCount").value(2))
                .andExpect(jsonPath("$.imageCount").value(2))
                .andExpect(jsonPath("$.photoUrls[0]").value("https://example.com/a.jpg"))
                .andExpect(jsonPath("$.imageUrls[0]").value("https://example.com/a.jpg"));

        var savedReservations = reservationRepository.findAll();
        assertThat(savedReservations).hasSize(1);
        assertThat(savedReservations.get(0).getRequirements()).isEqualTo("short nails");
    }

    @Test
    @DisplayName("예약 생성 요청에서 time 형식 오류는 400(INVALID_PARAMETER)으로 매핑된다")
    void createReservation_withInvalidTimeFormat_returns400() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("shopId", shopId);
        payload.put("staffId", staffId);
        payload.put("date", "2026-03-16");
        payload.put("time", "25:61");

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }
}
