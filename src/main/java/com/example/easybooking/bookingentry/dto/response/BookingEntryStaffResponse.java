package com.example.easybooking.bookingentry.dto.response;

import com.example.easybooking.staff.domain.Staff;
import lombok.Value;

@Value
public class BookingEntryStaffResponse {
    Long staffId;
    String name;

    public static BookingEntryStaffResponse from(Staff staff) {
        return new BookingEntryStaffResponse(staff.getId(), staff.getName());
    }
}
