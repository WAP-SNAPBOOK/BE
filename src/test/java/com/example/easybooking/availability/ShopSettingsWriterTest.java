package com.example.easybooking.availability;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.availability.domain.ShopSettings;
import com.example.easybooking.availability.repository.ShopSettingsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ShopSettingsWriterTest {

    @Autowired
    ShopSettingsRepository shopSettingsRepository;

    @Test
    @DisplayName("ShopSettings 저장 시 ID가 부여되고 기본값이 유지된다")
    void save_persistsShopSettingsAndAssignsId() {
        ShopSettingsWriter writer = new ShopSettingsWriter(shopSettingsRepository);

        ShopSettings saved = writer.save(ShopSettings.createDefault(1L));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getShopId()).isEqualTo(1L);
        assertThat(saved.getIntervalMinutes()).isEqualTo(30);
    }

    @Test
    @DisplayName("같은 shopId의 settings가 이미 있으면 중복 생성하지 않고 기존 row를 반환한다")
    void ensureDefaultByShopId_returnsExistingWithoutDuplicateInsert() {
        ShopSettingsWriter writer = new ShopSettingsWriter(shopSettingsRepository);
        ShopSettings existing = ShopSettings.createDefault(1L);
        existing.updateInterval(60);
        ShopSettings persisted = shopSettingsRepository.saveAndFlush(existing);

        ShopSettings result = writer.ensureDefaultByShopId(1L);

        assertThat(result.getId()).isEqualTo(persisted.getId());
        assertThat(result.getIntervalMinutes()).isEqualTo(60);
        assertThat(shopSettingsRepository.findAll()).hasSize(1);
    }
}
