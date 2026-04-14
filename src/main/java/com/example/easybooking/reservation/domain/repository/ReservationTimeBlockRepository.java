package com.example.easybooking.reservation.domain.repository;

import com.example.easybooking.reservation.domain.ReservationTimeBlock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationTimeBlockRepository extends JpaRepository<ReservationTimeBlock, Long> {
    boolean existsByStaffIdAndBlockStartAtIn(Long staffId, List<LocalDateTime> blockStartAt);

    List<ReservationTimeBlock> findByReservationIdOrderByBlockStartAtAsc(Long reservationId);

    void deleteByReservationIdIn(List<Long> reservationIds);

    List<ReservationTimeBlock> findByStaffIdAndBlockStartAtGreaterThanEqualAndBlockStartAtLessThan(
            Long staffId,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );
}
