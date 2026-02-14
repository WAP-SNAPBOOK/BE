package com.example.easybooking.availability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.easybooking.availability.domain.ShopSettings;
import com.example.easybooking.availability.exception.ShopSettingsNotFoundException;
import com.example.easybooking.availability.repository.ShopSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ShopSettingsReaderTest {

    @Autowired
    ShopSettingsRepository shopSettingsRepository;

    @Test
    void readByShopId_returnsShopSettings_whenExists() {
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(1L));
        ShopSettingsReader reader = new ShopSettingsReader(shopSettingsRepository);

        ShopSettings found = reader.readByShopId(1L);

        assertThat(found.getShopId()).isEqualTo(1L);
        assertThat(found.getIntervalMinutes()).isEqualTo(30);
    }

    @Test
    void readByShopId_throwsShopSettingsNotFoundException_whenNotExists() {
        ShopSettingsReader reader = new ShopSettingsReader(shopSettingsRepository);

        assertThatThrownBy(() -> reader.readByShopId(999L))
                .isInstanceOf(ShopSettingsNotFoundException.class);
    }
}
