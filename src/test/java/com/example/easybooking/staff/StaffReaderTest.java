package com.example.easybooking.staff;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.repository.StaffRepository;

import jakarta.persistence.EntityManager;

@DataJpaTest
class StaffReaderTest {

    @Autowired
    StaffRepository staffRepository;

    @Autowired
    EntityManager em;

    @Test
    void findByShopId_returnsStaffList() {
        staffRepository.saveAll(List.of(
                Staff.create(1L, "A"),
                Staff.create(1L, "B"),
                Staff.create(2L, "C")
        ));
        em.flush();
        em.clear();

        StaffReader staffReader = new StaffReader(staffRepository);
        List<Staff> found = staffReader.findByShopId(1L);

        assertThat(found).hasSize(2);
        assertThat(found).allSatisfy(staff -> assertThat(staff.getShopId()).isEqualTo(1L));
    }

    @Test
    void findByShopId_returnsEmptyList_whenNone() {
        StaffReader staffReader = new StaffReader(staffRepository);

        assertThat(staffReader.findByShopId(999L)).isEmpty();
    }
}

