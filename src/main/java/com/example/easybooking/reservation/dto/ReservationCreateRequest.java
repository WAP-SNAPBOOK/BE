package com.example.easybooking.reservation.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

// 고객이 입력하는 모든 예약 정보를 담음
// 날짜, 시간, 샵 ID, 사진 URL 등

@Data
public class ReservationCreateRequest {
    private Long shopId;
    private Long staffId;
    // private LocalDate date;
    // private LocalTime time;
    // private String designImageURL;

    // 추가로 필요한 정보 ?
    // 메모, 고객 전화번호 등
    // private String name;         // 고객 이름
    // private String phoneNumber;  // 고객 전화번호

    // 폼 데이터 필드 추가
    private Map<String, String> formData;
}
