package com.example.easybooking.form;

import com.example.easybooking.form.domain.Form;
import com.example.easybooking.form.domain.FormField;
import com.example.easybooking.form.domain.repository.FormRepository;
import com.example.easybooking.form.domain.repository.FormFieldRepository;
import com.example.easybooking.form.domain.type.FieldType;
import com.example.easybooking.form.dto.FormFieldPatchDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 서버 기동 시 기본 폼 템플릿을 DB에 생성
 */

@Component
@RequiredArgsConstructor
public class FormInitializer {

    private final FormRepository formRepository;
    private final FormFieldRepository formFieldRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeDefaultForm() {
        if (formRepository.findByName("시스템 기본 폼").isPresent()) {
            return;
        }

        // 기본 폼 (form name : 시스템 기본 폼) 생성
        Form defaultFormTemplate = Form.createFormTemplate();
        Form savedForm = formRepository.save(defaultFormTemplate);

        // 기본 폼 목록 생성 및 저장
        List<FormFieldPatchDto> defaultFieldsDtos = buildDefaultFieldsDtos();
        List<FormField> newFields = defaultFieldsDtos.stream()
                .map(dto -> FormField.createFrom(dto, savedForm))
                .collect(Collectors.toList());

        formFieldRepository.saveAll(newFields);
    }


    private List<FormFieldPatchDto> buildDefaultFieldsDtos() {
        return List.of(
                // 1. 이름
                createDto("name", "이름", FieldType.TEXT, true, 1, "예약자 이름", null),
                // 2. 전화번호
                createDto("phone", "전화번호", FieldType.TEXT, true, 2, "010xxxxxxxx", null),
                // 3. 날짜
                createDto("date", "날짜", FieldType.TEXT, true, 3, "2025-10-26", null),
                // 4. 예약 시간 (time)
                createDto("time", "예약 시간", FieldType.TEXT, true, 4, "15:30", null),
                // 5. 제거 유무
                createDto("removal", "제거 유무", FieldType.RADIO, true, 5, null, List.of("예", "아니오")),
                // 6. 손/발
                createDto("part", "손/발", FieldType.RADIO, true, 6, null, List.of("손", "발")),
                // 7. 래핑
                createDto("wrapping", "래핑 추가", FieldType.NUMBER, false, 7, "추가 래핑 수 (개)", null),
                // 8. 연장
                createDto("extend", "연장 추가", FieldType.NUMBER, false, 8, "추가 연장 수 (개)", null),
                // 9. 디자인 사진
                createDto("photo", "디자인 사진", FieldType.FILE, false, 9, "사진 첨부", null),
                // 10. 요구사항 텍스트
                createDto("requests", "요구사항", FieldType.TEXTAREA, false, 10, "자유롭게 입력해주세요", null)
        );
    }

    private FormFieldPatchDto createDto(String fieldId, String label, FieldType type, Boolean required, Integer order, String placeholder, List<String> options) {
        return FormFieldPatchDto.builder()
                .fieldId(fieldId)
                .label(label)
                .type(type)
                .required(required)
                .displayOrder(order)
                .placeholder(placeholder)
                .options(options)
                .operation(FormFieldPatchDto.PatchOperation.ADD)
                .build();
    }
}
