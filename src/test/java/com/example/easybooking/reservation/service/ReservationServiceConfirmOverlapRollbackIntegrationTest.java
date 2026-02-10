package com.example.easybooking.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.easybooking.errors.errorcode.ReservationErrorCode;
import com.example.easybooking.errors.exception.ReservationException;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.domain.ReservationTimeBlock;
import com.example.easybooking.reservation.domain.repository.ReservationRepository;
import com.example.easybooking.reservation.domain.repository.ReservationTimeBlockRepository;
import com.example.easybooking.reservation.dto.ReservationConfirmRequest;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ReservationServiceConfirmOverlapRollbackIntegrationTest {

    @Autowired
    ReservationService reservationService;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ReservationTimeBlockRepository reservationTimeBlockRepository;
    @Autowired
    EntityManager em;

    @MockitoBean
    UserReader userReader;
    @MockitoBean
    ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void clearData() {
        reservationTimeBlockRepository.deleteAll();
        reservationRepository.deleteAll();
        em.clear();
    }

    @Test
    void confirmReservation_whenTimeBlockAlreadyOccupied_rollsBackAndKeepsPendingStatus() {
        long ownerId = 10L;
        long staffId = 1L;

        when(userReader.read(ownerId))
                .thenReturn(User.createUser("provider-owner", "원장", "010", UserType.OWNER));

        Reservation reservation = Reservation.createReservation(
                100L, ownerId, 200L,
                LocalDate.of(2026, 2, 5),
                LocalTime.of(14, 0),
                "{}", List.of()
        );
        reservation.setStaffId(staffId);

        reservation = reservationRepository.saveAndFlush(reservation);
        final Long reservationId = reservation.getId();

        // given: 이미 동일 staff + 동일 시작 블록 점유 존재
        reservationTimeBlockRepository.saveAndFlush(
                ReservationTimeBlock.create(999L, staffId, LocalDateTime.of(2026, 2, 5, 14, 0))
        );

        ReservationConfirmRequest req = new ReservationConfirmRequest();
        req.setMessage("ok");
        req.setDurationMinutes(60);

        assertThatThrownBy(() -> reservationService.confirmReservation(reservationId, ownerId, req))
                .isInstanceOf(ReservationException.class)
                .satisfies(e -> {
                    ReservationException re = (ReservationException) e;
                    assertThat(re.getErrorCode()).isEqualTo(ReservationErrorCode.TIME_BLOCK_ALREADY_BOOKED);
                });

        // then: DB 재조회로 status가 PENDING인지 확인(영속성 컨텍스트 캐시 방지)
        em.clear();
        Reservation reloaded = reservationRepository.findById(reservation.getId()).orElseThrow();

        assertThat(reloaded.getStatus()).isEqualTo(Reservation.Status.PENDING);
    }

    @Test
    void confirmReservation_withRescheduledStartAt_createsBlocksStartingFromNewTime() {
        long ownerId = 10L;
        long staffId = 1L;

        when(userReader.read(ownerId))
                .thenReturn(User.createUser("provider-owner", "원장", "010", UserType.OWNER));
        when(userReader.read(200L))
                .thenReturn(User.createUser("provider-customer", "고객", "010", UserType.CUSTOMER));
        
        Reservation reservation = Reservation.createReservation(
                100L, ownerId, 200L,
                LocalDate.of(2026, 2, 5),
                LocalTime.of(14, 0),
                "{}", List.of()
        );
        reservation.setStaffId(staffId);

        reservation = reservationRepository.saveAndFlush(reservation);
        final Long reservationId = reservation.getId();

        ReservationConfirmRequest req = new ReservationConfirmRequest();
        req.setMessage("ok");
        req.setDurationMinutes(60);
        req.setStartAt(LocalTime.of(15, 0)); // reschedule

        reservationService.confirmReservation(reservationId, ownerId, req);

        em.clear();

        List<ReservationTimeBlock> blocks =
                reservationTimeBlockRepository.findByReservationIdOrderByBlockStartAtAsc(reservationId);

        assertThat(blocks).hasSize(6);
        assertThat(blocks).extracting(ReservationTimeBlock::getBlockStartAt)
                .containsExactly(
                        LocalDateTime.of(2026, 2, 5, 15, 0),
                        LocalDateTime.of(2026, 2, 5, 15, 10),
                        LocalDateTime.of(2026, 2, 5, 15, 20),
                        LocalDateTime.of(2026, 2, 5, 15, 30),
                        LocalDateTime.of(2026, 2, 5, 15, 40),
                        LocalDateTime.of(2026, 2, 5, 15, 50)
                );

        Reservation reloaded = reservationRepository.findById(reservationId).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(Reservation.Status.CONFIRMED);
        assertThat(reloaded.getTime()).isEqualTo(LocalTime.of(15, 0));
    }

    @Test
    void confirmReservation_withRescheduledStartAt_whenOccupied_rollsBackAndKeepsPending() {
        long ownerId = 10L;
        long staffId = 1L;

        when(userReader.read(ownerId))
                .thenReturn(User.createUser("provider-owner", "원장", "010", UserType.OWNER));

        Reservation reservation = Reservation.createReservation(
                100L, ownerId, 200L,
                LocalDate.of(2026, 2, 5),
                LocalTime.of(14, 0),
                "{}", List.of()
        );
        reservation.setStaffId(staffId);

        reservation = reservationRepository.saveAndFlush(reservation);
        final Long reservationId = reservation.getId();

        // given: 15:00 블록이 이미 점유된 상태
        reservationTimeBlockRepository.saveAndFlush(
                ReservationTimeBlock.create(999L, staffId, LocalDateTime.of(2026, 2, 5, 15, 0))
        );

        ReservationConfirmRequest req = new ReservationConfirmRequest();
        req.setMessage("ok");
        req.setDurationMinutes(60);
        req.setStartAt(LocalTime.of(15, 0)); // reschedule

        assertThatThrownBy(() -> reservationService.confirmReservation(reservationId, ownerId, req))
                .isInstanceOf(ReservationException.class)
                .satisfies(e -> {
                    ReservationException re = (ReservationException) e;
                    assertThat(re.getErrorCode()).isEqualTo(ReservationErrorCode.TIME_BLOCK_ALREADY_BOOKED);
                });

        em.clear();

        Reservation reloaded = reservationRepository.findById(reservationId).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(Reservation.Status.PENDING);
        assertThat(reloaded.getTime()).isEqualTo(LocalTime.of(14, 0));
    }
}