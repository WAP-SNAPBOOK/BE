package com.example.easybooking.bookingentry.dto.response;

import com.example.easybooking.shop.domain.Shop;
import java.util.List;
import lombok.Value;

@Value
public class BookingEntryResponse {
    Long shopId;
    String shopName;
    Long defaultStaffId;
    List<BookingEntryStaffResponse> staffs;

    public static BookingEntryResponse of(Shop shop, List<BookingEntryStaffResponse> staffs) {
        Long defaultStaffId = staffs.isEmpty() ? null : staffs.get(0).getStaffId();
        return new BookingEntryResponse(shop.getId(), shop.getBusinessName(), defaultStaffId, staffs);
    }
}
