package com.example.easybooking.reservation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.easybooking.reservation.ReservationReader;
import com.example.easybooking.reservation.ReservationWriter;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.dto.ReservationCustomerResponse;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.staff.StaffReader;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ReservationServiceGetCustomerReservationInChatUnitTest {

    @Mock private ReservationWriter reservationWriter;
    @Mock private ReservationReader reservationReader;
    @Mock private UserReader userReader;
    @Mock private ShopReader shopReader;
    @Mock private StaffReader staffReader;
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
                staffReader,
                objectMapper,
                eventPublisher
        );
    }

    @Test
    void getCustomerReservationInChat_예약0건이면_빈리스트_그리고_추가조회없음() {
        // given
        long customerId = 1L;
        long shopId = 2L;
        when(reservationReader.findByCustomerIdAndShopId(customerId, shopId)).thenReturn(List.of());

        // when
        List<ReservationCustomerResponse> result = reservationService.getCustomerReservationInChat(customerId, shopId);

        // then
        assertTrue(result.isEmpty());
        verifyNoInteractions(userReader);
        verifyNoInteractions(shopReader);
    }

    @Test
    void getCustomerReservationInChat_N건이어도_고객조회1회_샵조회1회_그리고_findByCustomerId는_호출되지않음() throws Exception {
        // given
        long customerId = 10L;
        long shopId = 20L;

        Map<String, String> formData = Map.of(
                "part", "손",
                "removal", "예",
                "requests", "요청",
                "extend", "0",
                "wrapping", "0"
        );
        String formJson = objectMapper.writeValueAsString(formData);

        Reservation r1 = Reservation.createReservation(
                shopId, 100L, customerId,
                LocalDate.parse("2026-01-10"), LocalTime.parse("10:00"),
                formJson, List.of()
        );
        Reservation r2 = Reservation.createReservation(
                shopId, 100L, customerId,
                LocalDate.parse("2026-01-11"), LocalTime.parse("11:00"),
                formJson, List.of()
        );

        when(reservationReader.findByCustomerIdAndShopId(customerId, shopId)).thenReturn(List.of(r1, r2));
        when(userReader.read(customerId)).thenReturn(User.createUser("provider-1", "고객", "01000000000", UserType.CUSTOMER));

        Shop shop = Mockito.mock(Shop.class);
        when(shop.getBusinessName()).thenReturn("샵");
        when(shopReader.read(shopId)).thenReturn(shop);

        // when
        List<ReservationCustomerResponse> result = reservationService.getCustomerReservationInChat(customerId, shopId);

        // then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(x -> "고객".equals(x.getCustomerName())));
        assertEquals(Set.of("샵"), result.stream().map(ReservationCustomerResponse::getShopName).collect(Collectors.toSet()));

        verify(userReader, times(1)).read(customerId);
        verify(shopReader, times(1)).read(shopId);
        verify(reservationReader, never()).findByCustomerId(anyLong());
    }
}

