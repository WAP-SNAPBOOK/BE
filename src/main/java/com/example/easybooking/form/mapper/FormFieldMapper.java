package com.example.easybooking.form.mapper;

import com.example.easybooking.form.FormFieldReader;
import com.example.easybooking.form.domain.Form;
import com.example.easybooking.form.domain.FormField;
import com.example.easybooking.form.dto.FormFieldDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FormFieldMapper {
    private final FormFieldReader formFieldReader;

    public static FormFieldDto toDto(FormField formField) {
        return FormFieldDto.builder()
                .fieldId(formField.getFieldId())
                .label(formField.getLabel())
                .type(formField.getType())
                .required(formField.getRequired())
                .build();
    }
    public List<FormFieldDto> getFormFieldDto(Form form){
        List<FormField> formFields = formFieldReader.read(form);
        return formFields.stream().map(FormFieldMapper::toDto).toList();
    }

}
