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
import com.example.easybooking.reservation.dto.ReservationOwnerResponse;
import com.example.easybooking.shop.ShopReader;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
class ReservationServiceGetShopReservationUnitTest {

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
    void getShopReservation_샵이없으면_빈리스트() {
        long ownerId = 1L;
        when(userReader.read(ownerId)).thenReturn(User.createUser("provider-owner", "원장", "010", UserType.OWNER));
        when(shopReader.findShopIdsByOwnerId(ownerId)).thenReturn(List.of());

        List<ReservationOwnerResponse> result = reservationService.getShopReservation(ownerId);

        assertTrue(result.isEmpty());
        verifyNoInteractions(reservationReader);
    }

    @Test
    void getShopReservation_N건이어도_고객조회는_IN1회_그리고_고객단건조회는_없음() throws Exception {
        long ownerId = 10L;
        when(userReader.read(ownerId)).thenReturn(User.createUser("provider-owner", "원장", "010", UserType.OWNER));

        // owner가 가진 shopIds
        when(shopReader.findShopIdsByOwnerId(ownerId)).thenReturn(List.of(100L));

        Map<String, String> formData = Map.of(
                "part", "손",
                "removal", "예",
                "requests", "요청",
                "extend", "0",
                "wrapping", "0"
        );
        String formJson = objectMapper.writeValueAsString(formData);

        long customer1Id = 101L;
        long customer2Id = 202L;

        Reservation r1 = Reservation.createReservation(
                100L, ownerId, customer1Id,
                LocalDate.parse("2026-01-10"), LocalTime.parse("10:00"), List.of()
        );
        Reservation r2 = Reservation.createReservation(
                100L, ownerId, customer2Id,
                LocalDate.parse("2026-01-11"), LocalTime.parse("11:00"), List.of()
        );
        Reservation r3 = Reservation.createReservation(
                100L, ownerId, customer1Id,
                LocalDate.parse("2026-01-12"), LocalTime.parse("12:00"), List.of()
        );

        when(reservationReader.findByShopIdIn(List.of(100L))).thenReturn(List.of(r1, r2, r3));

        // 배치 조회로 반환될 고객들
        User c1 = User.createUser("provider-c1", "고객1", "01011111111", UserType.CUSTOMER);
        User c2 = User.createUser("provider-c2", "고객2", "01022222222", UserType.CUSTOMER);

        // createUser는 id를 세팅하지 않으므로, id 매핑은 mock으로 보강
        // -> 여기서는 서비스가 Map 키로 getId()를 쓰므로 Mockito mock을 사용
        User c1Mock = org.mockito.Mockito.mock(User.class);
        when(c1Mock.getId()).thenReturn(customer1Id);
        when(c1Mock.getName()).thenReturn(c1.getName());
        when(c1Mock.getPhoneNumber()).thenReturn(c1.getPhoneNumber());

        User c2Mock = org.mockito.Mockito.mock(User.class);
        when(c2Mock.getId()).thenReturn(customer2Id);
        when(c2Mock.getName()).thenReturn(c2.getName());
        when(c2Mock.getPhoneNumber()).thenReturn(c2.getPhoneNumber());

        when(userReader.readAllByIds(anyList())).thenReturn(List.of(c1Mock, c2Mock));

        List<ReservationOwnerResponse> result = reservationService.getShopReservation(ownerId);

        assertEquals(3, result.size());
        assertEquals(
                Set.of("고객1", "고객2"),
                result.stream().map(ReservationOwnerResponse::getCustomerName).collect(Collectors.toSet())
        );

        // owner 권한 확인용 read 1회
        verify(userReader, times(1)).read(ownerId);
        // 고객 배치 조회 1회
        verify(userReader, times(1)).readAllByIds(anyList());
        // 고객 단건 조회는 없어야 함 (N+1 회귀 방지)
        verify(userReader, never()).read(customer1Id);
        verify(userReader, never()).read(customer2Id);
        // 과거 구현으로 회귀하면 발생하던 호출들 방지
        verify(shopReader, times(1)).findShopIdsByOwnerId(ownerId);
        verify(reservationReader, times(1)).findByShopIdIn(anyList());
        verify(reservationReader, never()).findByCustomerId(anyLong());
    }
}


