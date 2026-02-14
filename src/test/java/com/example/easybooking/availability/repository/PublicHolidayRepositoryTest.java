package com.example.easybooking.availability.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.availability.domain.PublicHoliday;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class PublicHolidayRepositoryTest {

    @Autowired
    PublicHolidayRepository publicHolidayRepository;

    @Test
    void existsByHolidayDate_returnsTrueWhenDateExists() {
        LocalDate newYearsDay = LocalDate.of(2026, 1, 1);

        publicHolidayRepository.saveAndFlush(PublicHoliday.create(newYearsDay, "신정"));

        boolean exists = publicHolidayRepository.existsByHolidayDate(newYearsDay);

        assertThat(exists).isTrue();
    }

    @Test
    void findByHolidayDateBetween_returnsOnlyDatesInRange() {
        publicHolidayRepository.saveAllAndFlush(List.of(
                PublicHoliday.create(LocalDate.of(2026, 1, 1), "신정"),
                PublicHoliday.create(LocalDate.of(2026, 2, 1), "테스트1"),
                PublicHoliday.create(LocalDate.of(2026, 2, 15), "테스트2"),
                PublicHoliday.create(LocalDate.of(2026, 3, 1), "삼일절")
        ));

        List<PublicHoliday> result = publicHolidayRepository.findByHolidayDateBetween(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        );

        assertThat(result)
                .extracting(PublicHoliday::getHolidayDate)
                .containsExactlyInAnyOrder(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 15));
    }
}
