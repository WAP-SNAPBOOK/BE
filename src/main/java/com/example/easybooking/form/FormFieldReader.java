package com.example.easybooking.form;

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
    public List<FormField> read(Form form){
        return formFieldRepository.findByForm(form);
    }

}
