package com.example.easybooking.bookingentry;

import com.example.easybooking.bookingentry.dto.response.BookingEntryResponse;
import com.example.easybooking.bookingentry.dto.response.BookingEntryStaffResponse;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.staff.StaffReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingEntryService {

    private final ShopReader shopReader;
    private final StaffReader staffReader;

    public BookingEntryResponse getBySlugOrCode(String slugOrCode) {
        return toResponse(shopReader.readBySlugOrPublicCode(slugOrCode));
    }

    public BookingEntryResponse getByShopId(Long shopId) {
        return toResponse(shopReader.read(shopId));
    }

    private BookingEntryResponse toResponse(Shop shop) {
        List<BookingEntryStaffResponse> staffs = staffReader.findByShopIdOrderByIdAsc(shop.getId()).stream()
                .map(BookingEntryStaffResponse::from)
                .toList();
        return BookingEntryResponse.of(shop, staffs);
    }
}
