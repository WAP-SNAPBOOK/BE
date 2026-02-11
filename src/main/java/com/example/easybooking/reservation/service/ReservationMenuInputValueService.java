package com.example.easybooking.reservation.service;

import com.example.easybooking.reservation.ReservationMenuInputValueWriter;
import com.example.easybooking.reservation.domain.ReservationMenuInputValue;
import com.example.easybooking.reservation.dto.MenuInputValueRequest;
import com.example.easybooking.shop.ShopMenuInputFieldReader;
import com.example.easybooking.shop.domain.InputType;
import com.example.easybooking.shop.domain.ShopMenuInputField;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationMenuInputValueService {

    private final ShopMenuInputFieldReader inputFieldReader;
    private final ReservationMenuInputValueWriter inputValueWriter;

    public List<ReservationMenuInputValue> saveInputValues(Long reservationMenuItemId,
                                                           Long shopMenuId,
                                                           List<MenuInputValueRequest> requests) {
        List<ShopMenuInputField> activeFields = inputFieldReader.findActiveByMenuId(shopMenuId);
        Map<Long, ShopMenuInputField> fieldMap = activeFields.stream()
                .collect(Collectors.toMap(ShopMenuInputField::getId, f -> f));

        Set<Long> activeFieldIds = fieldMap.keySet();

        // 제출된 fieldId가 해당 메뉴에 정의된 것인지 검증
        if (requests != null) {
            for (MenuInputValueRequest req : requests) {
                if (!activeFieldIds.contains(req.getFieldId())) {
                    throw new IllegalArgumentException(
                            "fieldId=" + req.getFieldId() + "는 해당 메뉴에 정의되지 않은 입력 필드입니다.");
                }
            }
        }

        Map<Long, MenuInputValueRequest> requestMap = (requests == null ? List.<MenuInputValueRequest>of() : requests)
                .stream()
                .collect(Collectors.toMap(MenuInputValueRequest::getFieldId, r -> r));

        List<ReservationMenuInputValue> values = new ArrayList<>();

        for (ShopMenuInputField field : activeFields) {
            MenuInputValueRequest req = requestMap.get(field.getId());

            // required 필드 누락 검증
            if (field.getRequired() && req == null) {
                throw new IllegalArgumentException(
                        "필수 입력 필드 '" + field.getLabel() + "'의 값이 누락되었습니다.");
            }

            if (req == null) {
                continue;
            }

            validateFieldValue(field, req);

            values.add(ReservationMenuInputValue.create(
                    reservationMenuItemId,
                    field.getId(),
                    field.getLabel(),
                    field.getInputType().name(),
                    req.getValueNumber(),
                    req.getValueText()
            ));
        }

        return inputValueWriter.saveAll(values);
    }

    private void validateFieldValue(ShopMenuInputField field, MenuInputValueRequest req) {
        if (field.getInputType() == InputType.NUMBER) {
            if (req.getValueNumber() == null && req.getValueText() != null) {
                throw new IllegalArgumentException(
                        "NUMBER 타입 필드 '" + field.getLabel() + "'에 숫자 값이 필요합니다.");
            }
            if (req.getValueNumber() != null) {
                validateNumberRange(field, req.getValueNumber());
                validateNumberStep(field, req.getValueNumber());
            }
        } else if (field.getInputType() == InputType.TEXT) {
            if (req.getValueText() == null && req.getValueNumber() != null) {
                throw new IllegalArgumentException(
                        "TEXT 타입 필드 '" + field.getLabel() + "'에 텍스트 값이 필요합니다.");
            }
            if (req.getValueText() != null) {
                validateTextLength(field, req.getValueText());
            }
        }
    }

    private void validateNumberRange(ShopMenuInputField field, BigDecimal value) {
        if (field.getMinValue() != null && value.compareTo(field.getMinValue()) < 0) {
            throw new IllegalArgumentException(
                    "필드 '" + field.getLabel() + "' 값이 최솟값(" + field.getMinValue() + ") 미만입니다.");
        }
        if (field.getMaxValue() != null && value.compareTo(field.getMaxValue()) > 0) {
            throw new IllegalArgumentException(
                    "필드 '" + field.getLabel() + "' 값이 최댓값(" + field.getMaxValue() + ")을 초과합니다.");
        }
    }

    private void validateNumberStep(ShopMenuInputField field, BigDecimal value) {
        if (field.getStepValue() == null) {
            return;
        }
        BigDecimal base = field.getMinValue() != null ? field.getMinValue() : BigDecimal.ZERO;
        BigDecimal diff = value.subtract(base);
        if (diff.remainder(field.getStepValue()).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException(
                    "필드 '" + field.getLabel() + "' 값이 step(" + field.getStepValue() + ") 단위에 맞지 않습니다.");
        }
    }

    private void validateTextLength(ShopMenuInputField field, String text) {
        if (field.getMaxLength() != null && text.length() > field.getMaxLength()) {
            throw new IllegalArgumentException(
                    "필드 '" + field.getLabel() + "' 텍스트가 최대 길이(" + field.getMaxLength() + ")를 초과합니다.");
        }
    }
}
