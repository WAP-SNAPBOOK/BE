package com.example.easybooking.shop.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.easybooking.shop.repository.TagRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class TagJpaMappingTest {

    @Autowired
    EntityManager em;

    @Autowired
    TagRepository tagRepository;

    @Test
    void canPersistAndLoadTag() {
        Tag tag = Tag.create("손관리");

        em.persist(tag);
        em.flush();
        em.clear();

        assertThat(tag.getId()).isNotNull();

        Tag found = em.find(Tag.class, tag.getId());
        assertThat(found.getName()).isEqualTo("손관리");
    }

    @Test
    void save_throwsException_whenDuplicateName() {
        tagRepository.saveAndFlush(Tag.create("손관리"));

        assertThatThrownBy(() -> tagRepository.saveAndFlush(Tag.create("손관리")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
