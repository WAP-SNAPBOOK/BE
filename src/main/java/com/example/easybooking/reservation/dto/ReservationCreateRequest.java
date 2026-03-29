package com.example.easybooking.reservation.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ReservationCreateRequest {
    private Long shopId;
    private Long staffId;

    // 레거시 폼 데이터 (dual-write 호환)
    private Map<String, String> formData;

    // 신규: 메뉴 선택 (nullable — 없으면 레거시 모드)
    private List<MenuSelectionRequest> menuSelections;
}
