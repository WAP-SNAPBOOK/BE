package com.example.easybooking.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.easybooking.reservation.ReservationReader;
import com.example.easybooking.reservation.ReservationTimeBlockWriter;
import com.example.easybooking.reservation.ReservationWriter;
import com.example.easybooking.reservation.TimeBlockGenerator;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.dto.ReservationCreateRequest;
import com.example.easybooking.reservation.dto.ReservationResponse;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.staff.StaffReader;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ReservationServiceCreateReservationNewFieldsUnitTest {

    @Mock ReservationWriter reservationWriter;
    @Mock ReservationReader reservationReader;
    @Mock UserReader userReader;
    @Mock ShopReader shopReader;
    @Mock StaffReader staffReader;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock TimeBlockGenerator timeBlockGenerator;
    @Mock ReservationTimeBlockWriter reservationTimeBlockWriter;
    @Mock ReservationMenuItemService reservationMenuItemService;
    @Mock ReservationMenuInputValueService reservationMenuInputValueService;
    @Mock com.example.easybooking.reservation.ReservationMenuItemReader menuItemReader;
    @Mock com.example.easybooking.reservation.ReservationMenuInputValueReader inputValueReader;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationWriter, reservationReader, userReader, shopReader, staffReader,
                new ObjectMapper(), eventPublisher, timeBlockGenerator,
                reservationTimeBlockWriter, reservationMenuItemService, reservationMenuInputValueService,
                menuItemReader, inputValueReader
        );
    }

    @Test
    void createReservation_worksWithExplicitFieldsOnly_withoutFormData() {
        long customerUserId = 200L;
        long shopId = 1L;
        long staffId = 10L;

        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setShopId(shopId);
        request.setStaffId(staffId);
        request.setDate(LocalDate.of(2026, 3, 20));
        request.setTime(LocalTime.of(10, 0));
        request.setRequirements("new-contract");
        request.setImageUrls(List.of("https://img/a.jpg", "https://img/b.jpg"));

        Shop shop = mock(Shop.class);
        when(shop.getOwnerId()).thenReturn(100L);
        when(shopReader.read(shopId)).thenReturn(shop);

        Staff staff = mock(Staff.class);
        when(staff.getShopId()).thenReturn(shopId);
        when(staffReader.read(staffId)).thenReturn(staff);

        when(userReader.read(customerUserId)).thenReturn(
                User.createUser("provider-customer", "고객", "010", UserType.CUSTOMER)
        );

        when(reservationWriter.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            var idField = Reservation.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(reservation, 999L);
            return reservation;
        });

        ReservationResponse response = reservationService.createReservation(request, customerUserId);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationWriter).save(captor.capture());
        Reservation saved = captor.getValue();

        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2026, 3, 20));
        assertThat(saved.getTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(saved.getDesignImageURLs()).hasSize(2);

        assertThat(response.getDate()).isEqualTo(LocalDate.of(2026, 3, 20));
        assertThat(response.getTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(response.getRequests()).isEqualTo("new-contract");
        assertThat(response.getPhotoCount()).isEqualTo(2);
    }
}
