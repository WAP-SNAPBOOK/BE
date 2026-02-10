package com.example.easybooking.staff.domain;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;

@DataJpaTest
class StaffJpaMappingTest {

    @Autowired
    EntityManager em;

    @Test
    void canPersistAndLoadStaff() {
        Staff staff = Staff.create(1L, "owner-name");

        em.persist(staff);
        em.flush();
        em.clear();

        assertThat(staff.getId()).isNotNull();

        Staff found = em.find(Staff.class, staff.getId());
        assertThat(found.getShopId()).isEqualTo(1L);
        assertThat(found.getName()).isEqualTo("owner-name");
    }
}

