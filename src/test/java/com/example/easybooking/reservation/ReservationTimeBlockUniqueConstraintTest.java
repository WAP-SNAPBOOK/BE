package com.example.easybooking.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.easybooking.reservation.domain.ReservationTimeBlock;
import com.example.easybooking.reservation.domain.repository.ReservationTimeBlockRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class ReservationTimeBlockUniqueConstraintTest {

    @Autowired
    ReservationTimeBlockRepository reservationTimeBlockRepository;

    @Test
    void save_throwsException_whenDuplicateStaffAndBlockStartAt() {
        reservationTimeBlockRepository.saveAndFlush(
                ReservationTimeBlock.create(1L, 10L, LocalDateTime.of(2026, 2, 5, 14, 0))
        );

        assertThatThrownBy(() -> reservationTimeBlockRepository.saveAndFlush(
                ReservationTimeBlock.create(2L, 10L, LocalDateTime.of(2026, 2, 5, 14, 0))
        )).isInstanceOf(DataIntegrityViolationException.class);
    }
}

