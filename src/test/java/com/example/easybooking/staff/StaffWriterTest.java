package com.example.easybooking.staff;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.repository.StaffRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class StaffWriterTest {

    @Autowired
    StaffRepository staffRepository;

    @Test
    void save_persistsStaffAndAssignsId() {
        StaffWriter staffWriter = new StaffWriter(staffRepository);

        Staff saved = staffWriter.save(Staff.create(1L, "기본"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getShopId()).isEqualTo(1L);
        assertThat(saved.getName()).isEqualTo("기본");
    }
}

