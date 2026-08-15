package com.example.easybooking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.easybooking.chat.ChatTopicPublisher;
import com.example.easybooking.chat.repository.MessageRepository;
import com.example.easybooking.notification.NotificationPublisher;
import com.example.easybooking.notification.NotificationWriter;
import com.example.easybooking.reservation.domain.repository.ReservationRepository;
import com.example.easybooking.reservation.dto.ReservationCreateRequest;
import com.example.easybooking.reservation.service.ReservationService;
import com.example.easybooking.shop.dto.request.CreateShopRequest;
import com.example.easybooking.shop.service.ShopService;
import com.example.easybooking.staff.repository.StaffRepository;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.example.easybooking.user.domain.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ReservationNotificationRollbackIntegrationTest {

    @MockitoBean private NotificationWriter notificationWriter;
    @MockitoBean private ChatTopicPublisher chatTopicPublisher;
    @MockitoBean private NotificationPublisher notificationPublisher;

    @Autowired private ReservationService reservationService;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ShopService shopService;
    @Autowired private StaffRepository staffRepository;

    @Test
    void 알림_저장이_실패하면_예약과_시스템메시지도_롤백한다() {
        Fixture fixture = createFixture();
        long reservationCountBefore = reservationRepository.count();
        long messageCountBefore = messageRepository.count();
        when(notificationWriter.saveReservationNotification(any(), any(), any()))
                .thenThrow(new IllegalStateException("notification persistence failed"));

        assertThatThrownBy(() -> reservationService.createReservation(
                createReservationRequest(fixture.shopId(), fixture.staffId()),
                fixture.customerId()
        )).isInstanceOf(IllegalStateException.class)
                .hasMessage("notification persistence failed");

        assertThat(reservationRepository.count()).isEqualTo(reservationCountBefore);
        assertThat(messageRepository.count()).isEqualTo(messageCountBefore);
        verifyNoInteractions(chatTopicPublisher, notificationPublisher);
    }

    private Fixture createFixture() {
        String suffix = UUID.randomUUID().toString();
        User owner = userRepository.save(User.createUser(
                "notification-rollback-owner-" + suffix,
                "점주",
                "01066666666",
                UserType.OWNER
        ));
        User customer = userRepository.save(User.createUser(
                "notification-rollback-customer-" + suffix,
                "고객",
                "01077777777",
                UserType.CUSTOMER
        ));

        CreateShopRequest shopRequest = CreateShopRequest.builder()
                .businessName("알림 롤백 매장-" + suffix)
                .address("테스트 주소")
                .businessNumber(suffix.substring(0, 12))
                .build();
        Long shopId = shopService.createShop(owner.getId(), shopRequest).getShopId();
        Long staffId = staffRepository.findFirstByShopIdOrderByIdAsc(shopId).orElseThrow().getId();
        return new Fixture(customer.getId(), shopId, staffId);
    }

    private ReservationCreateRequest createReservationRequest(Long shopId, Long staffId) {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setShopId(shopId);
        request.setStaffId(staffId);
        request.setDate(LocalDate.of(2027, 2, 1));
        request.setTime(LocalTime.of(14, 10));
        request.setImageUrls(List.of());
        return request;
    }

    private record Fixture(Long customerId, Long shopId, Long staffId) {
    }
}
