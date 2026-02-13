package com.example.easybooking.availability;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.availability.domain.ShopOperatingTime;
import com.example.easybooking.availability.domain.StaffOperatingTime;
import com.example.easybooking.availability.repository.ShopOperatingTimeRepository;
import com.example.easybooking.availability.repository.StaffOperatingTimeRepository;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.repository.StaffRepository;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class OperatingTimeResolverTest {

    @Autowired
    StaffRepository staffRepository;

    @Autowired
    ShopOperatingTimeRepository shopOperatingTimeRepository;

    @Autowired
    StaffOperatingTimeRepository staffOperatingTimeRepository;

    @Test
    void resolve_returnsShopDefaultTime_whenNoStaffOverride() {
        OperatingTimeResolver resolver = new OperatingTimeResolver(
                staffRepository,
                shopOperatingTimeRepository,
                staffOperatingTimeRepository
        );
        Staff staff = staffRepository.saveAndFlush(Staff.create(1L, "기본 직원"));
        shopOperatingTimeRepository.saveAndFlush(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(19, 0))
        );

        List<ShopOperatingTime> result = resolver.resolve(staff.getId(), DayOfWeek.MONDAY);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(result.get(0).getEndTime()).isEqualTo(LocalTime.of(19, 0));
    }

    @Test
    void resolve_returnsStaffOverrideTime_whenOverrideExists() {
        OperatingTimeResolver resolver = new OperatingTimeResolver(
                staffRepository,
                shopOperatingTimeRepository,
                staffOperatingTimeRepository
        );
        Staff staff = staffRepository.saveAndFlush(Staff.create(1L, "직원A"));
        shopOperatingTimeRepository.saveAndFlush(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(19, 0))
        );
        staffOperatingTimeRepository.saveAndFlush(
                StaffOperatingTime.create(staff.getId(), DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(17, 0))
        );

        List<ShopOperatingTime> result = resolver.resolve(staff.getId(), DayOfWeek.MONDAY);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(result.get(0).getEndTime()).isEqualTo(LocalTime.of(17, 0));
    }

    @Test
    void resolve_returnsEmpty_whenStaffIsOff() {
        OperatingTimeResolver resolver = new OperatingTimeResolver(
                staffRepository,
                shopOperatingTimeRepository,
                staffOperatingTimeRepository
        );
        Staff staff = staffRepository.saveAndFlush(Staff.create(1L, "직원B"));
        shopOperatingTimeRepository.saveAndFlush(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(19, 0))
        );
        staffOperatingTimeRepository.saveAndFlush(
                StaffOperatingTime.createOff(staff.getId(), DayOfWeek.MONDAY)
        );

        List<ShopOperatingTime> result = resolver.resolve(staff.getId(), DayOfWeek.MONDAY);

        assertThat(result).isEmpty();
    }

    @Test
    void resolve_returnsEmpty_whenShopHasNoTimeForDay() {
        OperatingTimeResolver resolver = new OperatingTimeResolver(
                staffRepository,
                shopOperatingTimeRepository,
                staffOperatingTimeRepository
        );
        Staff staff = staffRepository.saveAndFlush(Staff.create(1L, "직원C"));
        shopOperatingTimeRepository.saveAndFlush(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(19, 0))
        );

        List<ShopOperatingTime> result = resolver.resolve(staff.getId(), DayOfWeek.SUNDAY);

        assertThat(result).isEmpty();
    }

    @Test
    void resolve_returnsMultipleShopBlocks_whenShopHasBreakBlocks() {
        OperatingTimeResolver resolver = new OperatingTimeResolver(
                staffRepository,
                shopOperatingTimeRepository,
                staffOperatingTimeRepository
        );
        Staff staff = staffRepository.saveAndFlush(Staff.create(1L, "직원D"));
        shopOperatingTimeRepository.saveAllAndFlush(List.of(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(13, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(19, 0))
        ));

        List<ShopOperatingTime> result = resolver.resolve(staff.getId(), DayOfWeek.MONDAY);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(result.get(0).getEndTime()).isEqualTo(LocalTime.of(13, 0));
        assertThat(result.get(1).getStartTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(result.get(1).getEndTime()).isEqualTo(LocalTime.of(19, 0));
    }
}
