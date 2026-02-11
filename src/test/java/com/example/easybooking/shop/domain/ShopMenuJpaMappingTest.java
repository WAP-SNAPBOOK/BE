package com.example.easybooking.shop.domain;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ShopMenuJpaMappingTest {

    @Autowired
    EntityManager em;

    @Test
    void canPersistAndLoadShopMenu() {
        ShopMenu menu = ShopMenu.create(1L, "젤네일", "기본 젤네일", true, 0);

        em.persist(menu);
        em.flush();
        em.clear();

        assertThat(menu.getId()).isNotNull();

        ShopMenu found = em.find(ShopMenu.class, menu.getId());
        assertThat(found.getShopId()).isEqualTo(1L);
        assertThat(found.getName()).isEqualTo("젤네일");
        assertThat(found.getDescription()).isEqualTo("기본 젤네일");
        assertThat(found.getIsActive()).isTrue();
        assertThat(found.getSortOrder()).isEqualTo(0);
    }
}
