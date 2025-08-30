package com.example.easybooking.form.mapper;

import com.example.easybooking.form.domain.Form;
import com.example.easybooking.form.dto.FormResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FormMapper {
    private final FormFieldMapper formFieldMapper;
    public FormResponse toDto(Form form){
        return FormResponse.builder()
                .id(form.getId().toString())
                .name(form.getName())
                .title(form.getTitle())
                .formFields(formFieldMapper.getFormFieldDto(form))
                .build();
    }
}
