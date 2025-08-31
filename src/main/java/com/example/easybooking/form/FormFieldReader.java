package com.example.easybooking.form;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.easybooking.form.domain.Form;
import com.example.easybooking.form.domain.FormField;
import com.example.easybooking.form.domain.repository.FormFieldRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FormFieldReader {

    private final FormFieldRepository formFieldRepository;
    public List<FormField> read(Form form){
        return formFieldRepository.findByFormWithOptions(form);
    }

    public FormField readByFormAndFieldId(Form form, String fieldId){
        return formFieldRepository.findByFormAndFieldId(form, fieldId)
                .orElseThrow(() -> new IllegalArgumentException("해당 필드를 찾을 수 없습니다: " + fieldId));
    }
}
