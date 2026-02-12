package com.example.easybooking.availability.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.availability.domain.ScheduleType;
import com.example.easybooking.availability.domain.ShopSettings;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ShopSettingsRepositoryTest {

    @Autowired
    ShopSettingsRepository shopSettingsRepository;

    @Autowired
    EntityManager em;

    @Test
    void saveAndFindByShopId_returnsPersistedEntity() {
        ShopSettings settings = ShopSettings.createDefault(1L);

        ShopSettings saved = shopSettingsRepository.save(settings);
        em.flush();
        em.clear();

        Optional<ShopSettings> found = shopSettingsRepository.findByShopId(1L);

        assertThat(saved.getId()).isNotNull();
        assertThat(found).isPresent();
        assertThat(found.get().getShopId()).isEqualTo(1L);
        assertThat(found.get().getIntervalMinutes()).isEqualTo(30);
        assertThat(found.get().getScheduleType()).isEqualTo(ScheduleType.DAILY);
        assertThat(found.get().getBookingWindowDays()).isEqualTo(30);
        assertThat(found.get().getMinBookingLeadMinutes()).isEqualTo(60);
        assertThat(found.get().isPublicHolidayOff()).isFalse();
    }
}
