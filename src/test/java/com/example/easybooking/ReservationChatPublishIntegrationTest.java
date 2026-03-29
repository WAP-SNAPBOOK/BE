package com.example.easybooking;

import com.example.easybooking.chat.ChatTopicPublisher;
import com.example.easybooking.chat.domain.MessageType;
import com.example.easybooking.chat.dto.response.MessageResponse;
import com.example.easybooking.chat.repository.ChatRoomRepository;
import com.example.easybooking.chat.repository.MessageRepository;
import com.example.easybooking.reservation.dto.ReservationCreateRequest;
import com.example.easybooking.reservation.dto.ReservationResponse;
import com.example.easybooking.reservation.service.ReservationService;
import com.example.easybooking.shop.service.ShopService;
import com.example.easybooking.shop.dto.request.CreateShopRequest;
import com.example.easybooking.shop.dto.response.CreateShopResponse;
import com.example.easybooking.staff.repository.StaffRepository;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.example.easybooking.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class ReservationChatPublishIntegrationTest {

    // Spring Boot 3.4+ 권장(MockBean 대체)
    @MockitoBean
    private ChatTopicPublisher chatTopicPublisher;

    @Autowired private ReservationService reservationService;
    @Autowired private UserRepository userRepository;
    @Autowired private ShopService shopService;
    @Autowired private StaffRepository staffRepository;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private MessageRepository messageRepository;

    @Test
    void 예약접수_커밋후_시스템메시지저장_및_웹소켓발행() {
        // given: owner + customer
        User owner = userRepository.save(User.createUser(
                "provider-owner-1",
                "owner",
                "01000000000",
                UserType.OWNER
        ));

        User customer = userRepository.save(User.createUser(
                "provider-customer-1",
                "ascsc",
                "01095302336",
                UserType.CUSTOMER
        ));

        // given: shop
        CreateShopRequest createShopRequest = CreateShopRequest.builder()
                .businessName("test-shop")
                .address("test-address")
                .businessNumber("123-45-67890")
                .build();

        CreateShopResponse shopRes = shopService.createShop(owner.getId(), createShopRequest);
        Long shopId = shopRes.getShopId();

        Long staffId = staffRepository.findFirstByShopIdOrderByIdAsc(shopId)
                .orElseThrow()
                .getId();

        // given: reservation request
        ReservationCreateRequest req = new ReservationCreateRequest();
        req.setShopId(shopId);
        req.setStaffId(staffId);
        req.setDate(LocalDate.of(2025, 11, 12));
        req.setTime(LocalTime.of(17, 10));
        req.setRequirements("cscscsc");
        req.setImageUrls(List.of("KakaoTalk_20250511_191324465.jpg", "부경대 로고 2.jfif"));

        // when
        ReservationResponse res = reservationService.createReservation(req, customer.getId());
        Long reservationId = res.getId();

        // then: room 생성됐는지
        var room = chatRoomRepository.findByShopIdAndCustomerId(shopId, customer.getId()).orElseThrow();
        Long roomId = room.getId();

        // then: 시스템 메시지가 DB에 저장됐는지
        var savedMsg = messageRepository.findAll().stream()
                .filter(m -> reservationId.equals(m.getReservationId()))
                .findFirst()
                .orElseThrow();

        assertEquals(MessageType.RESERVATION_CREATED, savedMsg.getMessageType());
        assertEquals(roomId, savedMsg.getChatRoomId());

        // then: 웹소켓 발행(우리 Publisher 호출) 확인 - convertAndSend 오버로딩 문제 회피
        verify(chatTopicPublisher).publishToRoom(
                eq(roomId),
                argThat(p -> p instanceof MessageResponse mr
                        && mr.getMessageType() == MessageType.RESERVATION_CREATED
                        && reservationId.equals(mr.getReservationId())
                        && roomId.equals(mr.getRoomId())
                )
        );
    }
}
