package com.example.easybooking.reservation.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationMenuItemResponse {
    private final Long shopMenuId;
    private final String menuNameSnapshot;
    private final String tagNameSnapshot;
    private final Long priceSnapshot;
    private final Integer sortOrder;
    private final List<ReservationMenuInputValueResponse> inputValues;
}
