package com.example.easybooking.shop;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.shop.domain.ShopMenu;
import com.example.easybooking.shop.repository.ShopMenuRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ShopMenuWriterTest {

    @Autowired
    ShopMenuRepository shopMenuRepository;

    @Test
    void save_persistsShopMenuAndAssignsId() {
        ShopMenuWriter writer = new ShopMenuWriter(shopMenuRepository);

        ShopMenu saved = writer.save(ShopMenu.create(1L, "젤네일", "기본 젤네일", 50000L, true, 0));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getShopId()).isEqualTo(1L);
        assertThat(saved.getName()).isEqualTo("젤네일");
        assertThat(saved.getPrice()).isEqualTo(50000L);
    }
}
