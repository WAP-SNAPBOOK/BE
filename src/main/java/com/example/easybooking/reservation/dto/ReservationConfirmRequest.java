package com.example.easybooking.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReservationConfirmRequest {
    // 수락 시 전달사항 필수로 받음
    @NotBlank(message = "수락 시 고객에게 전달할 메시지는 필수 입력 사항입니다.")
    private String message;

    // 확정 시 예약 기간(분). 30분 단위(30, 60, 90, ...) 정책은 별도 검증으로 강제한다.
    private Integer durationMinutes;

}
