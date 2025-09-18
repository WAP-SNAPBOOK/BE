package com.example.easybooking.form.dto.request;

import com.example.easybooking.form.dto.FormFieldPatchDto;
import com.example.easybooking.form.dto.FormMetadataPatchDto;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FormPatchRequest {

    // 폼 메타데이터 부분 업데이트
    private FormMetadataPatchDto metadata;

    // 필드별 업데이트 작업
    @Valid
    private List<FormFieldPatchDto> fieldOperations;

}