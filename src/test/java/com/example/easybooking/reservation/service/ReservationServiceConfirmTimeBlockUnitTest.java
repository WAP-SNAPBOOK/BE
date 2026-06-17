package com.example.easybooking.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.easybooking.errors.errorcode.ReservationErrorCode;
import com.example.easybooking.errors.exception.ReservationException;
import com.example.easybooking.reservation.ReservationReader;
import com.example.easybooking.reservation.ReservationTimeBlockWriter;
import com.example.easybooking.reservation.ReservationWriter;
import com.example.easybooking.reservation.TimeBlockGenerator;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.domain.ReservationTimeBlock;
import com.example.easybooking.reservation.dto.ReservationConfirmRequest;
import com.example.easybooking.reservation.event.ReservationEvent;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.staff.StaffReader;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;


@ExtendWith(MockitoExtension.class)
class ReservationServiceConfirmTimeBlockUnitTest {

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
    @Mock
    private ReservationTimeBlockWriter reservationTimeBlockWriter;

    @Spy
    private TimeBlockGenerator timeBlockGenerator = new TimeBlockGenerator();

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void confirmReservation_creates10MinuteBlocks() {
        long reservationId = 1L;
        long ownerId = 10L;
        long staffId = 1L;

        ReservationConfirmRequest request = new ReservationConfirmRequest();
        request.setMessage("ok");
        request.setDurationMinutes(60);

        when(userReader.read(ownerId))
                .thenReturn(User.createUser("provider-owner", "원장", "010", UserType.OWNER));

        Reservation reservation = Reservation.createReservation(
                100L, ownerId, 200L,
                LocalDate.of(2026, 2, 5),
                LocalTime.of(14, 0), List.of()
        );
        reservation.setStaffId(staffId);
        reservation.setStartAt(LocalDateTime.of(2026, 2, 5, 14, 0));

        when(reservationReader.getById(reservationId)).thenReturn(reservation);
        when(userReader.read(reservation.getCustomerId()))
                .thenReturn(User.createUser("provider-customer", "고객", "010", UserType.CUSTOMER));

        // 실행
        reservationService.confirmReservation(reservationId, ownerId, request);

        ArgumentCaptor<List<ReservationTimeBlock>> captor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<ReservationEvent> eventCaptor = ArgumentCaptor.forClass(ReservationEvent.class);

        verify(reservationTimeBlockWriter).allocateOrThrowOnConflict(captor.capture(), eq(staffId));
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        List<ReservationTimeBlock> blocks = captor.getValue();
        assertThat(blocks).hasSize(6);
        assertThat(blocks).allSatisfy(block -> assertThat(block.getStaffId()).isEqualTo(staffId));
        assertThat(blocks).extracting(ReservationTimeBlock::getBlockStartAt)
                .containsExactly(
                        LocalDateTime.of(2026, 2, 5, 14, 0),
                        LocalDateTime.of(2026, 2, 5, 14, 10),
                        LocalDateTime.of(2026, 2, 5, 14, 20),
                        LocalDateTime.of(2026, 2, 5, 14, 30),
                        LocalDateTime.of(2026, 2, 5, 14, 40),
                        LocalDateTime.of(2026, 2, 5, 14, 50)
                );
        assertThat(eventCaptor.getValue().durationMinutes()).isEqualTo(60);
    }

    @Test
    void confirmReservation_whenWriterThrowsTimeSlotAlreadyBooked_propagatesReservationException() {
        long reservationId = 1L;
        long ownerId = 10L;
        long staffId = 1L;

        ReservationConfirmRequest request = new ReservationConfirmRequest();
        request.setMessage("ok");
        request.setDurationMinutes(60);

        when(userReader.read(ownerId))
                .thenReturn(User.createUser("provider-owner", "원장", "010", UserType.OWNER));

        Reservation reservation = Reservation.createReservation(
                100L, ownerId, 200L,
                LocalDate.of(2026, 2, 5),
                LocalTime.of(14, 0), List.of()
        );
        reservation.setStaffId(staffId);
        reservation.setStartAt(LocalDateTime.of(2026, 2, 5, 14, 0));

        when(reservationReader.getById(reservationId)).thenReturn(reservation);

        doThrow(new ReservationException(ReservationErrorCode.TIME_BLOCK_ALREADY_BOOKED))
                .when(reservationTimeBlockWriter)
                .allocateOrThrowOnConflict(anyList(), eq(staffId));

        assertThatThrownBy(() -> reservationService.confirmReservation(reservationId, ownerId, request))
                .isInstanceOf(ReservationException.class)
                .satisfies(e -> {
                    ReservationException re = (ReservationException) e;
                    assertThat(re.getErrorCode()).isEqualTo(ReservationErrorCode.TIME_BLOCK_ALREADY_BOOKED);
                });
    }
}
