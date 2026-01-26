package com.example.easybooking.reservation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.easybooking.reservation.ReservationReader;
import com.example.easybooking.reservation.ReservationWriter;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.dto.ReservationOwnerResponse;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ReservationServiceGetReservationsByCustomerInShopUnitTest {

    @Mock private ReservationWriter reservationWriter;
    @Mock private ReservationReader reservationReader;
    @Mock private UserReader userReader;
    @Mock private ShopReader shopReader;
    @Mock private ApplicationEventPublisher eventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationWriter,
                reservationReader,
                userReader,
                shopReader,
                objectMapper,
                eventPublisher
        );
    }

    @Test
    void getReservationsByCustomerInShop_예약0건이면_빈리스트_그리고_고객조회없음() {
        long ownerId = 1L;
        long shopId = 2L;
        long customerId = 3L;

        when(shopReader.isShopOwnedBy(shopId, ownerId)).thenReturn(true);
        when(reservationReader.findByShopIdAndCustomerId(shopId, customerId)).thenReturn(List.of());

        List<ReservationOwnerResponse> result = reservationService.getReservationsByCustomerInShop(ownerId, shopId, customerId);

        assertTrue(result.isEmpty());
        verifyNoInteractions(userReader);
    }

    @Test
    void getReservationsByCustomerInShop_N건이어도_고객조회1회만() throws Exception {
        long ownerId = 10L;
        long shopId = 20L;
        long customerId = 30L;

        when(shopReader.isShopOwnedBy(shopId, ownerId)).thenReturn(true);

        Map<String, String> formData = Map.of(
                "part", "손",
                "removal", "예",
                "requests", "요청",
                "extend", "0",
                "wrapping", "0"
        );
        String formJson = objectMapper.writeValueAsString(formData);

        Reservation r1 = Reservation.createReservation(
                shopId, ownerId, customerId,
                LocalDate.parse("2026-01-10"), LocalTime.parse("10:00"),
                formJson, List.of()
        );
        Reservation r2 = Reservation.createReservation(
                shopId, ownerId, customerId,
                LocalDate.parse("2026-01-11"), LocalTime.parse("11:00"),
                formJson, List.of()
        );
        when(reservationReader.findByShopIdAndCustomerId(shopId, customerId)).thenReturn(List.of(r1, r2));

        when(userReader.read(customerId)).thenReturn(User.createUser("provider-c", "고객", "01000000000", UserType.CUSTOMER));

        List<ReservationOwnerResponse> result = reservationService.getReservationsByCustomerInShop(ownerId, shopId, customerId);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(x -> "고객".equals(x.getCustomerName())));

        verify(userReader, times(1)).read(customerId);
        verify(userReader, never()).readAllByIds(org.mockito.ArgumentMatchers.anyList());
        verify(shopReader, times(1)).isShopOwnedBy(shopId, ownerId);
        verify(reservationReader, times(1)).findByShopIdAndCustomerId(shopId, customerId);
    }
}

