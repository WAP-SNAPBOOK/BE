package com.example.easybooking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.example.easybooking.chat.ChatTopicPublisher;
import com.example.easybooking.chat.domain.Message;
import com.example.easybooking.chat.domain.MessageType;
import com.example.easybooking.chat.dto.response.MessageResponse;
import com.example.easybooking.chat.repository.ChatRoomRepository;
import com.example.easybooking.chat.repository.MessageRepository;
import com.example.easybooking.chat.service.MessageService;
import com.example.easybooking.errors.errorcode.ChatErrorCode;
import com.example.easybooking.errors.exception.ChatException;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.domain.repository.ReservationRepository;
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
class ReservationMessageDeliveryIntegrationTest {

    @MockitoBean
    private ChatTopicPublisher chatTopicPublisher;

    @Autowired private ReservationService reservationService;
    @Autowired private MessageService messageService;
    @Autowired private UserRepository userRepository;
    @Autowired private ShopService shopService;
    @Autowired private StaffRepository staffRepository;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private ReservationRepository reservationRepository;

    @BeforeEach
    void resetPublisher() {
        reset(chatTopicPublisher);
    }

    @Test
    void 예약과_시스템메시지를_커밋한_뒤_웹소켓을_발행한다() {
        Fixture fixture = createFixture();

        ReservationResponse response = reservationService.createReservation(
                createReservationRequest(fixture.shopId(), fixture.staffId()),
                fixture.customerId()
        );

        Reservation reservation = reservationRepository.findById(response.getId()).orElseThrow();
        var chatRoom = chatRoomRepository
                .findByShopIdAndCustomerId(fixture.shopId(), fixture.customerId())
                .orElseThrow();
        Message message = findReservationMessage(reservation.getId());

        assertThat(message.getMessageType()).isEqualTo(MessageType.RESERVATION_CREATED);
        assertThat(message.getChatRoomId()).isEqualTo(chatRoom.getId());
        verify(chatTopicPublisher).publishToRoom(
                eq(chatRoom.getId()),
                argThat(payload -> payload instanceof MessageResponse messageResponse
                        && reservation.getId().equals(messageResponse.getReservationId())
                        && message.getId().equals(messageResponse.getMessageId()))
        );
    }

    @Test
    void 웹소켓_발행이_실패해도_예약과_시스템메시지는_유지한다() {
        Fixture fixture = createFixture();
        doThrow(new IllegalStateException("websocket unavailable"))
                .when(chatTopicPublisher)
                .publishToRoom(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());

        ReservationResponse response = reservationService.createReservation(
                createReservationRequest(fixture.shopId(), fixture.staffId()),
                fixture.customerId()
        );

        assertThat(reservationRepository.findById(response.getId())).isPresent();
        assertThat(findReservationMessage(response.getId()).getMessageType())
                .isEqualTo(MessageType.RESERVATION_CREATED);
    }

    @Test
    void 마지막_수신_ID_이후_메시지를_오름차순으로_조회한다() {
        Fixture fixture = createFixture();
        ReservationResponse response = reservationService.createReservation(
                createReservationRequest(fixture.shopId(), fixture.staffId()),
                fixture.customerId()
        );
        Message systemMessage = findReservationMessage(response.getId());

        Message second = messageRepository.saveAndFlush(
                Message.create(systemMessage.getChatRoomId(), fixture.ownerId(), "두 번째")
        );
        Message third = messageRepository.saveAndFlush(
                Message.create(systemMessage.getChatRoomId(), fixture.customerId(), "세 번째")
        );

        List<MessageResponse> messages = messageService.getMessageHistory(
                systemMessage.getChatRoomId(),
                fixture.customerId(),
                null,
                systemMessage.getId(),
                50
        );

        assertThat(messages).extracting(MessageResponse::getMessageId)
                .containsExactly(second.getId(), third.getId());
    }

    @Test
    void 과거와_이후_커서를_동시에_요청하면_거부한다() {
        Fixture fixture = createFixture();
        ReservationResponse response = reservationService.createReservation(
                createReservationRequest(fixture.shopId(), fixture.staffId()),
                fixture.customerId()
        );
        Message systemMessage = findReservationMessage(response.getId());

        assertThatThrownBy(() -> messageService.getMessageHistory(
                systemMessage.getChatRoomId(),
                fixture.customerId(),
                systemMessage.getId(),
                systemMessage.getId(),
                50
        )).isInstanceOfSatisfying(ChatException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ChatErrorCode.MESSAGE_CURSOR_CONFLICT));
    }

    private Message findReservationMessage(Long reservationId) {
        return messageRepository.findAll().stream()
                .filter(message -> reservationId.equals(message.getReservationId()))
                .findFirst()
                .orElseThrow();
    }

    private Fixture createFixture() {
        String suffix = UUID.randomUUID().toString();
        User owner = userRepository.save(User.createUser(
                "owner-" + suffix,
                "점주",
                "01000000000",
                UserType.OWNER
        ));
        User customer = userRepository.save(User.createUser(
                "customer-" + suffix,
                "고객",
                "01011111111",
                UserType.CUSTOMER
        ));

        CreateShopRequest shopRequest = CreateShopRequest.builder()
                .businessName("매장-" + suffix)
                .address("테스트 주소")
                .businessNumber(suffix.substring(0, 12))
                .build();
        Long shopId = shopService.createShop(owner.getId(), shopRequest).getShopId();
        Long staffId = staffRepository.findFirstByShopIdOrderByIdAsc(shopId).orElseThrow().getId();
        return new Fixture(owner.getId(), customer.getId(), shopId, staffId);
    }

    private ReservationCreateRequest createReservationRequest(Long shopId, Long staffId) {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setShopId(shopId);
        request.setStaffId(staffId);
        request.setDate(LocalDate.of(2027, 1, 10));
        request.setTime(LocalTime.of(14, 10));
        request.setImageUrls(List.of());
        return request;
    }

    private record Fixture(Long ownerId, Long customerId, Long shopId, Long staffId) {
    }
}
