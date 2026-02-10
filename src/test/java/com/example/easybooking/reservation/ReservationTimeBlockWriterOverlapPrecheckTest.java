package com.example.easybooking.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.easybooking.errors.errorcode.ReservationErrorCode;
import com.example.easybooking.errors.exception.ReservationException;
import com.example.easybooking.reservation.domain.ReservationTimeBlock;
import com.example.easybooking.reservation.domain.repository.ReservationTimeBlockRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ReservationTimeBlockWriterOverlapPrecheckTest {

    @Autowired
    ReservationTimeBlockRepository reservationTimeBlockRepository;

    @Test
    void allocateOrThrowOnConflict_whenOverlapExists_throwsTimeBlockAlreadyBooked() {
        ReservationTimeBlockWriter writer = new ReservationTimeBlockWriter(reservationTimeBlockRepository);

        long staffId = 10L;
        LocalDateTime startAt = LocalDateTime.of(2026, 2, 5, 14, 0);

        // given: 이미 점유(같은 staffId + 같은 blockStartAt)
        reservationTimeBlockRepository.saveAndFlush(
                ReservationTimeBlock.create(999L, staffId, startAt)
        );

        // when: 같은 시간대(14:00~) 블록들 할당 시도
        List<ReservationTimeBlock> blocks = List.of(
                ReservationTimeBlock.create(1L, staffId, startAt),
                ReservationTimeBlock.create(1L, staffId, startAt.plusMinutes(10))
        );

        // then
        assertThatThrownBy(() -> writer.allocateOrThrowOnConflict(blocks, staffId))
                .isInstanceOf(ReservationException.class)
                .satisfies(e -> {
                    ReservationException re = (ReservationException) e;
                    assertThat(re.getErrorCode()).isEqualTo(ReservationErrorCode.TIME_BLOCK_ALREADY_BOOKED);
                });
    }
}