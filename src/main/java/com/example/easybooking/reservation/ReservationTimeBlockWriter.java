package com.example.easybooking.reservation;

import com.example.easybooking.errors.errorcode.ReservationErrorCode;
import com.example.easybooking.errors.exception.ReservationException;
import com.example.easybooking.reservation.domain.ReservationTimeBlock;
import com.example.easybooking.reservation.domain.repository.ReservationTimeBlockRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationTimeBlockWriter {

    private final ReservationTimeBlockRepository reservationTimeBlockRepository;

    private static final String UQ_STAFF_BLOCK_START_AT = "uq_rtb_staff_block_start_at";

    public List<ReservationTimeBlock> saveAllAndFlush(List<ReservationTimeBlock> blocks) {
        return reservationTimeBlockRepository.saveAllAndFlush(blocks);
    }

    public void allocateOrThrowOnConflict(List<ReservationTimeBlock> blocks, Long staffId) {
        List<LocalDateTime> blockStarts = extractBlockStarts(blocks);

        ensureNoOverlapOrThrow(staffId, blockStarts);

        saveAllAndFlushOrThrowOnUniqueConflict(blocks);
    }

    private List<LocalDateTime> extractBlockStarts(List<ReservationTimeBlock> blocks) {
        return blocks.stream()
                .map(ReservationTimeBlock::getBlockStartAt)
                .toList();
    }

    private void ensureNoOverlapOrThrow(Long staffId, List<LocalDateTime> blockStarts) {
        if (blockStarts.isEmpty()) {
            return;
        }

        boolean overlap = reservationTimeBlockRepository
                .existsByStaffIdAndBlockStartAtIn(staffId, blockStarts);

        if (overlap) {
            throw new ReservationException(ReservationErrorCode.TIME_BLOCK_ALREADY_BOOKED);
        }
    }

    private void saveAllAndFlushOrThrowOnUniqueConflict(List<ReservationTimeBlock> blocks) {
        try {
            reservationTimeBlockRepository.saveAllAndFlush(blocks);
        } catch (DataIntegrityViolationException e) {
            if (DbConstraintUtils.isUniqueConstraintViolation(e, UQ_STAFF_BLOCK_START_AT)) {
                throw new ReservationException(ReservationErrorCode.TIME_BLOCK_ALREADY_BOOKED);
            }
            throw e;
        }
    }


}

