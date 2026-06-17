package com.example.easybooking.shop.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.easybooking.shop.repository.ShopMenuRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class ShopMenuUniqueConstraintTest {

    @Autowired
    ShopMenuRepository shopMenuRepository;

    @Test
    void save_throwsException_whenDuplicateShopIdAndName() {
        shopMenuRepository.saveAndFlush(
                ShopMenu.create(1L, "젤네일", "기본 젤네일", 50000L, true, 0)
        );

        assertThatThrownBy(() -> shopMenuRepository.saveAndFlush(
                ShopMenu.create(1L, "젤네일", "다른 설명", 60000L, true, 1)
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_allowsSameNameInDifferentShop() {
        shopMenuRepository.saveAndFlush(
                ShopMenu.create(1L, "젤네일", null, true, 0)
        );
        shopMenuRepository.saveAndFlush(
                ShopMenu.create(2L, "젤네일", null, true, 0)
        );
        // 예외 없이 성공
    }
}
