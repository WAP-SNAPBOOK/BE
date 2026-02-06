package com.example.easybooking.staff;

import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.exception.StaffNotFoundException;
import com.example.easybooking.staff.repository.StaffRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaffReader {

    private final StaffRepository staffRepository;
    private static final String DEFAULT_STAFF_NAME = "기본";

    public List<Staff> findByShopId(Long shopId) {
        return staffRepository.findByShopId(shopId);
    }

    public Staff getDefaultStaffByShopId(Long shopId) {
        return staffRepository.findByShopIdAndName(shopId, DEFAULT_STAFF_NAME)
                .orElseThrow(StaffNotFoundException::new);
    }
}

