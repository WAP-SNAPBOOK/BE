package com.example.easybooking.shop.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.easybooking.shop.repository.ShopMenuTagRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class ShopMenuTagJpaMappingTest {

    @Autowired
    EntityManager em;

    @Autowired
    ShopMenuTagRepository shopMenuTagRepository;

    @Test
    void canPersistAndLoadShopMenuTag() {
        ShopMenuTag smt = ShopMenuTag.createResolved(1L, 1L, 11L);

        em.persist(smt);
        em.flush();
        em.clear();

        assertThat(smt.getId()).isNotNull();

        ShopMenuTag found = em.find(ShopMenuTag.class, smt.getId());
        assertThat(found.getShopMenuId()).isEqualTo(1L);
        assertThat(found.getTagId()).isEqualTo(1L);
        assertThat(found.getShopTagId()).isEqualTo(11L);
    }

    @Test
    void save_throwsException_whenDuplicateMenuAndTag() {
        shopMenuTagRepository.saveAndFlush(ShopMenuTag.create(1L, 1L));

        assertThatThrownBy(() -> shopMenuTagRepository.saveAndFlush(ShopMenuTag.create(1L, 1L)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_throwsException_whenDuplicateMenuAndShopTag() {
        shopMenuTagRepository.saveAndFlush(ShopMenuTag.createResolved(1L, 1L, 10L));

        assertThatThrownBy(() -> shopMenuTagRepository.saveAndFlush(ShopMenuTag.createResolved(1L, 2L, 10L)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_allowsSameMenuDifferentTag() {
        shopMenuTagRepository.saveAndFlush(ShopMenuTag.create(1L, 1L));
        shopMenuTagRepository.saveAndFlush(ShopMenuTag.create(1L, 2L));
        // no exception
    }

    @Test
    void save_allowsSameTagDifferentMenu() {
        shopMenuTagRepository.saveAndFlush(ShopMenuTag.create(1L, 1L));
        shopMenuTagRepository.saveAndFlush(ShopMenuTag.create(2L, 1L));
        // no exception
    }
}
