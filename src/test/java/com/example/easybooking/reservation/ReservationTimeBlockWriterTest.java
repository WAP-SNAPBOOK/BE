package com.example.easybooking.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.reservation.domain.ReservationTimeBlock;
import com.example.easybooking.reservation.domain.repository.ReservationTimeBlockRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ReservationTimeBlockWriterTest {

    @Autowired
    ReservationTimeBlockRepository reservationTimeBlockRepository;

    @Test
    void saveAll_persistsAllBlocksAndAssignsIds() {
        ReservationTimeBlockWriter writer = new ReservationTimeBlockWriter(reservationTimeBlockRepository);

        List<ReservationTimeBlock> blocks = List.of(
                ReservationTimeBlock.create(1L, 10L, LocalDateTime.of(2026, 2, 5, 14, 0)),
                ReservationTimeBlock.create(1L, 10L, LocalDateTime.of(2026, 2, 5, 14, 30))
        );

        List<ReservationTimeBlock> saved = writer.saveAll(blocks);

        assertThat(saved).hasSize(2);
        assertThat(saved).allSatisfy(block -> assertThat(block.getId()).isNotNull());
    }
}

