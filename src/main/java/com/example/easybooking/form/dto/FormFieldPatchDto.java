package com.example.easybooking.form.dto;

import java.util.List;

import com.example.easybooking.form.domain.type.FieldType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FormFieldPatchDto {

    @NotNull(message = "작업 타입은 필수입니다")
    private PatchOperation operation;

    @NotBlank(message = "필드 ID는 필수입니다")
    private String fieldId;

    // ADD, UPDATE 작업 시에만 사용
    private String label;
    private FieldType type;
    private Boolean required;
    private String placeholder;
    private Integer minValue;
    private Integer maxValue;
    private String acceptTypes;
    private Integer displayOrder;
    private List<String> options;

    public enum PatchOperation {
        ADD,      // 새 필드 추가
        UPDATE,   // 기존 필드 수정
        DELETE,   // 필드 삭제
    }
}


