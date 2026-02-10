package com.example.easybooking.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.easybooking.errors.errorcode.ReservationErrorCode;
import com.example.easybooking.errors.exception.ReservationException;
import com.example.easybooking.reservation.domain.ReservationTimeBlock;
import com.example.easybooking.reservation.domain.repository.ReservationTimeBlockRepository;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ReservationTimeBlockWriterRaceMappingUnitTest {

    @Mock
    ReservationTimeBlockRepository reservationTimeBlockRepository;


    @Test
    void allocateOrThrowOnConflict_whenUniqueConstraintViolated_throwsTimeBlockAlreadyBooked() {
        ReservationTimeBlockWriter writer = new ReservationTimeBlockWriter(reservationTimeBlockRepository);

        long staffId = 10L;
        LocalDateTime startAt = LocalDateTime.of(2026, 2, 5, 14, 0);
        List<ReservationTimeBlock> blocks = List.of(
                ReservationTimeBlock.create(1L, staffId, startAt),
                ReservationTimeBlock.create(1L, staffId, startAt.plusMinutes(10))
        );

        // 사전조회는 통과(레이스 상황 가정)
        when(reservationTimeBlockRepository.existsByStaffIdAndBlockStartAtIn(eq(staffId), anyList()))
                .thenReturn(false);

        // 저장 시점에 DB 유니크 제약 위반 발생
        SQLException sqlEx = new SQLException(
                "Duplicate entry for key 'uq_rtb_staff_block_start_at'",
                "23000",
                1062
        );
        ConstraintViolationException hibEx =
                new ConstraintViolationException("unique violation", sqlEx, "uq_rtb_staff_block_start_at");
        DataIntegrityViolationException springEx =
                new DataIntegrityViolationException("data integrity", hibEx);

        when(reservationTimeBlockRepository.saveAllAndFlush(anyList()))
                .thenThrow(springEx);

        assertThatThrownBy(() -> writer.allocateOrThrowOnConflict(blocks, staffId))
                .isInstanceOf(ReservationException.class)
                .satisfies(e -> {
                    ReservationException re = (ReservationException) e;
                    assertThat(re.getErrorCode()).isEqualTo(ReservationErrorCode.TIME_BLOCK_ALREADY_BOOKED);
                });
    }
}