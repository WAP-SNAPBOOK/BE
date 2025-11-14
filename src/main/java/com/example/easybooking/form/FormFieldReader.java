package com.example.easybooking.form;

import com.example.easybooking.errors.errorcode.FormErrorCode;
import com.example.easybooking.errors.exception.FormException;
import com.example.easybooking.form.domain.Form;
import com.example.easybooking.form.domain.FormField;
import com.example.easybooking.form.domain.repository.FormFieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FormFieldReader {

    private final FormFieldRepository formFieldRepository;

    public List<FormField> read(Form form) {
        return formFieldRepository.findByFormWithOptions(form);
    }

    public FormField readByFormAndFieldId(Form form, String fieldId) {
        return formFieldRepository.findByFormAndFieldId(form, fieldId)
                .orElseThrow(() -> new FormException(FormErrorCode.FORM_FIELD_NOT_FOUND));
    }
}
