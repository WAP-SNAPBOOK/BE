package com.example.easybooking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.example.easybooking.chat.ChatTopicPublisher;
import com.example.easybooking.errors.errorcode.NotificationErrorCode;
import com.example.easybooking.errors.exception.NotificationException;
import com.example.easybooking.notification.NotificationPublisher;
import com.example.easybooking.notification.domain.Notification;
import com.example.easybooking.notification.domain.NotificationType;
import com.example.easybooking.notification.dto.NotificationResponse;
import com.example.easybooking.notification.repository.NotificationRepository;
import com.example.easybooking.notification.service.NotificationService;
import com.example.easybooking.reservation.domain.repository.ReservationRepository;
import com.example.easybooking.reservation.dto.ReservationConfirmRequest;
import com.example.easybooking.reservation.dto.ReservationCreateRequest;
import com.example.easybooking.reservation.dto.ReservationResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ReservationNotificationIntegrationTest {

    @MockitoBean private ChatTopicPublisher chatTopicPublisher;
    @MockitoBean private NotificationPublisher notificationPublisher;

    @Autowired private ReservationService reservationService;
    @Autowired private NotificationService notificationService;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ShopService shopService;
    @Autowired private StaffRepository staffRepository;

    @BeforeEach
    void resetPublishers() {
        reset(chatTopicPublisher, notificationPublisher);
    }

    @Test
    void 고객이_예약하면_점주_알림을_저장하고_커밋_후_발행한다() {
        Fixture fixture = createFixture();

        ReservationResponse reservation = reservationService.createReservation(
                createReservationRequest(fixture.shopId(), fixture.staffId(), LocalDate.of(2027, 2, 1)),
                fixture.customerId()
        );

        Notification notification = notificationRepository.findAll().stream()
                .filter(candidate -> reservation.getId().equals(candidate.getReservationId()))
                .findFirst()
                .orElseThrow();

        assertThat(notification.getRecipientId()).isEqualTo(fixture.ownerId());
        assertThat(notification.getActorId()).isEqualTo(fixture.customerId());
        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.RESERVATION_CREATED);
        assertThat(notification.getChatRoomId()).isNotNull();
        assertThat(notification.getMessageId()).isNotNull();
        assertThat(notificationService.getUnreadCount(fixture.ownerId()).unreadCount()).isEqualTo(1);
        assertThat(notificationService.getUnreadCount(fixture.customerId()).unreadCount()).isZero();

        verify(notificationPublisher).publishToUser(
                eq(fixture.ownerId()),
                argThat(payload -> payload.notificationId().equals(notification.getId()))
        );
    }

    @Test
    void 알림을_최신순으로_조회하고_읽음_처리한다() {
        Fixture fixture = createFixture();
        reservationService.createReservation(
                createReservationRequest(fixture.shopId(), fixture.staffId(), LocalDate.of(2027, 2, 1)),
                fixture.customerId()
        );
        reservationService.createReservation(
                createReservationRequest(fixture.shopId(), fixture.staffId(), LocalDate.of(2027, 2, 2)),
                fixture.customerId()
        );

        List<NotificationResponse> notifications = notificationService.getNotifications(
                fixture.ownerId(),
                null,
                20
        );

        assertThat(notifications).hasSize(2);
        assertThat(notifications.get(0).notificationId())
                .isGreaterThan(notifications.get(1).notificationId());

        Long latestId = notifications.get(0).notificationId();
        List<NotificationResponse> older = notificationService.getNotifications(
                fixture.ownerId(),
                latestId,
                20
        );
        assertThat(older).extracting(NotificationResponse::notificationId)
                .containsExactly(notifications.get(1).notificationId());

        notificationService.markAsRead(latestId, fixture.ownerId());
        assertThat(notificationService.getUnreadCount(fixture.ownerId()).unreadCount()).isEqualTo(1);

        notificationService.markAllAsRead(fixture.ownerId());
        assertThat(notificationService.getUnreadCount(fixture.ownerId()).unreadCount()).isZero();
    }

    @Test
    void 점주가_예약을_확정하면_고객에게_알림을_저장한다() {
        Fixture fixture = createFixture();
        ReservationResponse reservation = reservationService.createReservation(
                createReservationRequest(fixture.shopId(), fixture.staffId(), LocalDate.of(2027, 2, 1)),
                fixture.customerId()
        );
        ReservationConfirmRequest confirmRequest = new ReservationConfirmRequest();
        confirmRequest.setMessage("예약 확정 안내");
        confirmRequest.setDurationMinutes(60);
        confirmRequest.setStartAt(LocalTime.of(14, 0));

        reservationService.confirmReservation(reservation.getId(), fixture.ownerId(), confirmRequest);

        List<NotificationResponse> customerNotifications = notificationService.getNotifications(
                fixture.customerId(),
                null,
                20
        );
        assertThat(customerNotifications).singleElement().satisfies(notification -> {
            assertThat(notification.notificationType()).isEqualTo(NotificationType.RESERVATION_CONFIRMED);
            assertThat(notification.reservationId()).isEqualTo(reservation.getId());
            assertThat(notification.read()).isFalse();
        });
    }

    @Test
    void 다른_사용자의_알림은_읽음_처리할_수_없다() {
        Fixture fixture = createFixture();
        reservationService.createReservation(
                createReservationRequest(fixture.shopId(), fixture.staffId(), LocalDate.of(2027, 2, 1)),
                fixture.customerId()
        );
        Long notificationId = notificationService.getNotifications(fixture.ownerId(), null, 20)
                .get(0)
                .notificationId();

        assertThatThrownBy(() -> notificationService.markAsRead(notificationId, fixture.customerId()))
                .isInstanceOfSatisfying(NotificationException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
    }

    @Test
    void 알림_웹소켓_발행이_실패해도_예약과_알림은_유지한다() {
        Fixture fixture = createFixture();
        doThrow(new IllegalStateException("notification websocket unavailable"))
                .when(notificationPublisher)
                .publishToUser(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());

        ReservationResponse reservation = reservationService.createReservation(
                createReservationRequest(fixture.shopId(), fixture.staffId(), LocalDate.of(2027, 2, 1)),
                fixture.customerId()
        );

        assertThat(reservationRepository.findById(reservation.getId())).isPresent();
        assertThat(notificationRepository.findAll())
                .anyMatch(notification -> reservation.getId().equals(notification.getReservationId()));
    }

    private Fixture createFixture() {
        String suffix = UUID.randomUUID().toString();
        User owner = userRepository.save(User.createUser(
                "notification-owner-" + suffix,
                "점주",
                "01044444444",
                UserType.OWNER
        ));
        User customer = userRepository.save(User.createUser(
                "notification-customer-" + suffix,
                "고객",
                "01055555555",
                UserType.CUSTOMER
        ));

        CreateShopRequest shopRequest = CreateShopRequest.builder()
                .businessName("알림 매장-" + suffix)
                .address("테스트 주소")
                .businessNumber(suffix.substring(0, 12))
                .build();
        Long shopId = shopService.createShop(owner.getId(), shopRequest).getShopId();
        Long staffId = staffRepository.findFirstByShopIdOrderByIdAsc(shopId).orElseThrow().getId();
        return new Fixture(owner.getId(), customer.getId(), shopId, staffId);
    }

    private ReservationCreateRequest createReservationRequest(
            Long shopId,
            Long staffId,
            LocalDate date
    ) {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setShopId(shopId);
        request.setStaffId(staffId);
        request.setDate(date);
        request.setTime(LocalTime.of(14, 10));
        request.setImageUrls(List.of());
        return request;
    }

    private record Fixture(Long ownerId, Long customerId, Long shopId, Long staffId) {
    }
}
