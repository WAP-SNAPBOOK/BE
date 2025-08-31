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

}
