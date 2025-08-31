package com.example.easybooking.form.domain;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.easybooking.form.FormFieldReader;
import com.example.easybooking.form.FormReader;
import com.example.easybooking.form.domain.repository.FormFieldRepository;
import com.example.easybooking.form.domain.repository.FormRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FormCopyService {

    private final FormReader formReader;
    private final FormFieldReader formFieldReader;
    private final FormRepository formRepository;
    private final FormFieldRepository formFieldRepository;

    public void copyDefaultFormToShop(Long shopId) {
        // 기본 폼과 필드들을 복사하여 새 매장에 할당
        Form defaultForm = formReader.readDefaultForm();
        Form newForm = Form.createForm(defaultForm, shopId);
        Form savedForm = formRepository.save(newForm);

        List<FormField> defaultFields = formFieldReader.read(defaultForm);
        List<FormField> newFields = defaultFields.stream()
                .map(field -> FormField.copyFrom(field, savedForm))
                .collect(Collectors.toList());
        formFieldRepository.saveAll(newFields);
    }

}