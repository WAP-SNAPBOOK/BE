package com.example.easybooking.form;

import com.example.easybooking.errors.errorcode.FormErrorCode;
import com.example.easybooking.errors.exception.FormException;
import com.example.easybooking.form.domain.Form;
import com.example.easybooking.form.domain.repository.FormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FormReader {
    private final FormRepository formRepository;

    public Form read(Long id) {
        return formRepository.findById(id)
                .orElseThrow(() -> new FormException(FormErrorCode.FORM_NOT_FOUND));
    }

    public Form readDefaultForm() {
        List<Form> forms = formRepository.findByName("시스템 기본 폼");
        if (forms.isEmpty()) {
            throw new FormException(FormErrorCode.DEFAULT_FORM_NOT_FOUND);
        }
        return forms.get(0);
    }

    public Form readByShopId(Long shopId) {
        return formRepository.findByShopId(shopId)
                .orElseThrow(() -> new FormException(FormErrorCode.FORM_FOR_SHOP_NOT_FOUND));
    }
}
