package com.example.easybooking.staff;

import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaffWriter {

    private final StaffRepository staffRepository;

    public Staff save(Staff staff) {
        return staffRepository.save(staff);
    }
}

