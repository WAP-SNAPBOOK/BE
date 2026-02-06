package com.example.easybooking.staff;

import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.repository.StaffRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaffReader {

    private final StaffRepository staffRepository;

    public List<Staff> findByShopId(Long shopId) {
        return staffRepository.findByShopId(shopId);
    }

    public Staff getDefaultStaffByShopId(Long shopId) {
        throw new UnsupportedOperationException("TODO");
    }
}

