package com.example.easybooking.availability;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.availability.domain.ShopSettings;
import com.example.easybooking.availability.repository.ShopSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ShopSettingsWriterTest {

    @Autowired
    ShopSettingsRepository shopSettingsRepository;

    @Test
    void save_persistsShopSettingsAndAssignsId() {
        ShopSettingsWriter writer = new ShopSettingsWriter(shopSettingsRepository);

        ShopSettings saved = writer.save(ShopSettings.createDefault(1L));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getShopId()).isEqualTo(1L);
        assertThat(saved.getIntervalMinutes()).isEqualTo(30);
    }
}
