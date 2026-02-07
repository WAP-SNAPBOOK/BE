package com.example.easybooking.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.easybooking.errors.exception.ReservationException;
import com.example.easybooking.reservation.ReservationReader;
import com.example.easybooking.reservation.ReservationWriter;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.dto.ReservationCreateRequest;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.staff.StaffReader;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.exception.StaffIdNotFoundException;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ReservationServiceCreateReservationStaffIdValidationTest {

    @Mock
    private ReservationWriter reservationWriter;

    @Mock
    private ReservationReader reservationReader;

    @Mock
    private UserReader userReader;

    @Mock
    private ShopReader shopReader;

    @Mock
    private StaffReader staffReader;

    @Mock
    private ApplicationEventPublisher eventPublisher;

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
    void createReservation_savesSelectedStaffId() {
        long customerUserId = 10L;
        long shopId = 100L;
        long ownerUserId = 20L;
        long staffId = 1L;

        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setShopId(shopId);
        request.setStaffId(staffId);
        request.setFormData(Map.of(
                "date", "2026-02-05",
                "time", "14:00"
        ));

        Shop shop = mock(Shop.class);
        when(shop.getOwnerId()).thenReturn(ownerUserId);
        when(shopReader.read(shopId)).thenReturn(shop);

        when(userReader.read(customerUserId)).thenReturn(
                User.createUser("provider-customer", "고객", "010", UserType.CUSTOMER)
        );

        when(staffReader.read(staffId)).thenReturn(Staff.create(shopId, "직원"));
        when(reservationWriter.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reservationService.createReservation(request, customerUserId);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationWriter).save(captor.capture());
        assertThat(captor.getValue().getStaffId()).isEqualTo(staffId);
    }

    @Test
    void createReservation_throwsException_whenStaffIdMissing() {
        long customerUserId = 10L;
        long shopId = 100L;
        long ownerUserId = 20L;

        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setShopId(shopId);
        request.setStaffId(null);
        request.setFormData(Map.of(
                "date", "2026-02-05",
                "time", "14:00"
        ));

        Shop shop = mock(Shop.class);
        when(shop.getOwnerId()).thenReturn(ownerUserId);
        when(shopReader.read(shopId)).thenReturn(shop);

        assertThatThrownBy(() -> reservationService.createReservation(request, customerUserId))
                .isInstanceOf(ReservationException.class);
    }

    @Test
    void createReservation_throwsException_whenStaffBelongsToDifferentShop() {
        long customerUserId = 10L;
        long shopId = 100L;
        long ownerUserId = 20L;
        long staffId = 1L;

        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setShopId(shopId);
        request.setStaffId(staffId);
        request.setFormData(Map.of(
                "date", "2026-02-05",
                "time", "14:00"
        ));

        Shop shop = mock(Shop.class);
        when(shop.getOwnerId()).thenReturn(ownerUserId);
        when(shopReader.read(shopId)).thenReturn(shop);

        when(staffReader.read(staffId)).thenReturn(Staff.create(shopId + 1, "직원"));

        assertThatThrownBy(() -> reservationService.createReservation(request, customerUserId))
                .isInstanceOf(ReservationException.class);
    }

    @Test
    void createReservation_throwsException_whenStaffIdDoesNotExist() {
        long customerUserId = 10L;
        long shopId = 100L;
        long ownerUserId = 20L;
        long staffId = 999L;

        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setShopId(shopId);
        request.setStaffId(staffId);
        request.setFormData(Map.of(
                "date", "2026-02-05",
                "time", "14:00"
        ));

        Shop shop = mock(Shop.class);
        when(shop.getOwnerId()).thenReturn(ownerUserId);
        when(shopReader.read(shopId)).thenReturn(shop);

        when(staffReader.read(staffId)).thenThrow(new StaffIdNotFoundException());

        assertThatThrownBy(() -> reservationService.createReservation(request, customerUserId))
                .isInstanceOf(ReservationException.class);
    }

    @Test
    void createReservation_throwsException_whenTimeNotOn30MinuteBoundary() {
        long customerUserId = 10L;
        long shopId = 100L;
        long ownerUserId = 20L;
        long staffId = 1L;

        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setShopId(shopId);
        request.setStaffId(staffId);
        request.setFormData(Map.of(
                "date", "2026-02-05",
                "time", "14:10"
        ));

        Shop shop = mock(Shop.class);
        when(shop.getOwnerId()).thenReturn(ownerUserId);
        when(shopReader.read(shopId)).thenReturn(shop);

        when(userReader.read(customerUserId)).thenReturn(
                User.createUser("provider-customer", "고객", "010", UserType.CUSTOMER)
        );

        when(staffReader.read(staffId)).thenReturn(Staff.create(shopId, "직원"));
        when(reservationWriter.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> reservationService.createReservation(request, customerUserId))
                .isInstanceOf(ReservationException.class);
    }
}

