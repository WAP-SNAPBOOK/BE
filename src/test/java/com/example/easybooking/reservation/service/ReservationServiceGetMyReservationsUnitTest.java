package com.example.easybooking.reservation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
class ReservationServiceGetMyReservationsUnitTest {

    @Mock private ReservationWriter reservationWriter;
    @Mock private ReservationReader reservationReader;
    @Mock private UserReader userReader;
    @Mock private ShopReader shopReader;
    @Mock private StaffReader staffReader;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void getMyReservations_예약0건이면_빈리스트_그리고_추가조회없음() {
        // given
        long customerId = 1L;
        when(reservationReader.findByCustomerId(customerId)).thenReturn(List.of());

        // when
        List<ReservationCustomerResponse> result = reservationService.getMyReservations(customerId);

        // then
        assertTrue(result.isEmpty());
        verifyNoInteractions(userReader);
        verifyNoInteractions(shopReader);
    }

    @Test
    void getMyReservations_N건이어도_user1회_shop배치1회_단건샵조회0회() throws Exception {
        // given
        long customerId = 10L;

        Map<String, String> formData = Map.of(
                "part", "손",
                "removal", "예",
                "requests", "요청",
                "extend", "0",
                "wrapping", "0"
        );
        String formJson = objectMapper.writeValueAsString(formData);

        Reservation r1 = Reservation.createReservation(
                1L, 100L, customerId,
                LocalDate.parse("2026-01-10"), LocalTime.parse("10:00"), List.of("https://img/a.jpg")
        );
        Reservation r2 = Reservation.createReservation(
                2L, 200L, customerId,
                LocalDate.parse("2026-01-11"), LocalTime.parse("11:00"), List.of()
        );
        Reservation r3 = Reservation.createReservation(
                1L, 100L, customerId,
                LocalDate.parse("2026-01-12"), LocalTime.parse("12:00"), List.of()
        );
        r1.setRequirements("요청1");
        r1.setDurationMinutes(60);

        when(reservationReader.findByCustomerId(customerId)).thenReturn(List.of(r1, r2, r3));
        when(userReader.read(customerId)).thenReturn(User.createUser("provider-1", "고객", "01000000000", UserType.CUSTOMER));

        Shop s1 = Mockito.mock(Shop.class);
        when(s1.getId()).thenReturn(1L);
        when(s1.getBusinessName()).thenReturn("샵1");

        Shop s2 = Mockito.mock(Shop.class);
        when(s2.getId()).thenReturn(2L);
        when(s2.getBusinessName()).thenReturn("샵2");

        when(shopReader.readAllByIds(anyList())).thenReturn(List.of(s1, s2));

        // when
        List<ReservationCustomerResponse> result = reservationService.getMyReservations(customerId);

        // then: 결과 기본 검증
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(x -> "고객".equals(x.getCustomerName())));
        assertEquals(
                Set.of("샵1", "샵2"),
                result.stream().map(ReservationCustomerResponse::getShopName).collect(Collectors.toSet())
        );
        assertEquals("요청1", result.get(0).getRequirements());
        assertEquals(60, result.get(0).getDurationMinutes());
        assertEquals(1, result.get(0).getImageCount());
        assertEquals(List.of("https://img/a.jpg"), result.get(0).getImageUrls());
        assertEquals(1, result.get(0).getPhotoCount());

        // then: N+1 방지 핵심 검증(호출 횟수)
        verify(userReader, times(1)).read(customerId);
        verify(shopReader, times(1)).readAllByIds(anyList());
        verify(shopReader, never()).read(anyLong());
    }
}
