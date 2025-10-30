package com.example.easybooking.reservation.service;

import com.example.easybooking.reservation.ReservationReader;
import com.example.easybooking.reservation.ReservationWriter;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.dto.ReservationConfirmRequest;
import com.example.easybooking.reservation.dto.ReservationCreateRequest;
import com.example.easybooking.reservation.dto.ReservationRejectRequest;
import com.example.easybooking.reservation.dto.ReservationResponse;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);


    private final ReservationWriter reservationWriter;   // Writer 주입
    private final ReservationReader reservationReader;   // Reader 주입
    private final UserReader userReader;
    private final ShopReader shopReader;

    private final ObjectMapper objectMapper;


    /**
     * 1. 고객 예약 신청 로직 (Create)
     */
    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request, Long userId) {
        Long customerUserId = userId;
        Long shopId = request.getShopId();

        Shop shop = shopReader.read(shopId);
        Long ownerUserId = shop.getOwnerId();

        Map<String, String> formData = request.getFormData();
        String formDataJson;

        try {
            formDataJson = objectMapper.writeValueAsString(formData);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("폼 데이터 처리 중 JSON 변환 오류가 발생했습니다.");
        }

        // 날짜 추출 및 변환
        String dateString = formData.get("date");
        if (dateString == null) {
            throw new IllegalArgumentException(("예약 날짜(date)는 필수 폼 항목입니다."));
        }
        LocalDate date = LocalDate.parse(dateString);

        // 시간 추출 및 변환
        String timeString = formData.get("time");
        if (timeString == null) {
            throw new IllegalArgumentException("예약 시간(time)은 필수 폼 항목입니다.");
        }
        LocalTime time = LocalTime.parse(timeString);

        // 디자인 사진 URL 추출
        String photoJsonString = formData.get("photo");
        List<String> designImageURLs;

        if (photoJsonString == null || photoJsonString.trim().isEmpty()) {
            designImageURLs = Collections.emptyList();
        } else {
            try {
                designImageURLs = objectMapper.readValue(photoJsonString, new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("첨부 사진(photo) 데이터 형식이 올바르지 않습니다. JSON 배열 형식이어야 합니다.");
            }
        }


        Reservation newReservation = Reservation.createReservation(
                shopId,
                ownerUserId,
                customerUserId,
                date,
                time,
                formDataJson,
                designImageURLs
        );

        Reservation savedReservation = reservationWriter.save(newReservation);
        return new ReservationResponse(savedReservation);
    }

    /**
     * 2. 원장님 예약 수락(확정) 로직 (Update)
     * - OWNER 권한 검증
     * - 샵 일치 검증 (본인 샵 예약만 처리 가능)
     * - Reservation 엔티티의 confirm() 메소드 호출
     */
    @Transactional
    public void confirmReservation(Long reservationId, Long ownerUserId, ReservationConfirmRequest request) {
        // 1. 원장님(OWNER) 권한 검증
        User user = userReader.read(ownerUserId);

        if (user.getUserType() != UserType.OWNER) {
            throw new IllegalStateException("예약 확정 권한이 없습니다. (OWNER만 가능)");
        }

        // 2. 예약 엔티티 조회 및 샵 일치 여부 확인
        Reservation reservation = reservationReader.getById(reservationId);

        // 3. 샵 일치 검증: 예약된 샵 ID(Reservation.shopId)와 현재 원장님 ID가 일치하는지 확인
        if (!reservation.getOwnerUserId().equals(ownerUserId)) {
            throw new AccessDeniedException("해당 샵의 예약에 대한 처리 권한이 없습니다.");
        }

        // 4. 엔티티 상태 변경
        log.info("예약 ID: {} - 상태 변경 전: {}", reservationId, reservation.getStatus());
        reservation.confirm(request.getMessage());
        log.info("예약 ID: {} - 상태 변경 후: {}", reservationId, reservation.getStatus());
    }

    /**
     * 3. 예약 거절 로직 (Update)
     */
    @Transactional
    public void rejectReservation(Long reservationId, Long ownerUserId, ReservationRejectRequest request) {
        // 1. 원장님(owner) 권한 검증
        User user = userReader.read(ownerUserId);
        if (user.getUserType() != UserType.OWNER) {
            throw new IllegalStateException("예약 거절 권한이 없습니다. (OWNER만 가능)");
        }

        // 2. 예약 엔티티 조회 및 샵 일치 여부 확인
        Reservation reservation = reservationReader.getById(reservationId);

        // 3. 샵 일치 검정
        if (!reservation.getOwnerUserId().equals(ownerUserId)) {
            throw new AccessDeniedException("해당 샵의 예약에 대한 처리 권한이 없습니다.");
        }

        // 4. 엔티티 상태 변경
        log.info("예약 ID: {} - 거절 전 상태: {}", reservationId, reservation.getStatus());
        reservation.reject(request.getReason());
        log.info("예약 ID: {} - 거절 후 상태: {}", reservationId, reservation.getStatus());

        // TODO: 고객에게 거절 알림
    }

    /**
     * 고객 전용: 내 예약 내역 조회
     */


    // 3. 예약 취소 및 거절 로직 (Update)
    // 4. 고객 예약 내역 조회 로직 (Read)
    // 5. 원장님 샵 예약 목록 조회 로직 (Read)
}
