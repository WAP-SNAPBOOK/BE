package com.example.easybooking.reservation.dto;

import com.example.easybooking.common.validation.MultipleOf;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationConfirmRequest {
    // 수락 시 전달사항 필수로 받음
    @NotBlank(message = "수락 시 고객에게 전달할 메시지는 필수 입력 사항입니다.")
    private String message;

    @NotNull(message = "예약 시간은 필수 입력 사항입니다.")
    @MultipleOf(base = 10, message = "예약 시간은 10분 단위여야 합니다.")
    private Integer durationMinutes;

    /**
     * 확정 시 점주가 시작 시간을 변경(reschedule)할 수 있다.
     * <p>
     * - nullable: 미전달 시 기존 예약 시간으로 확정
     * - 형식: "HH:mm"
     */
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startAt;

}
