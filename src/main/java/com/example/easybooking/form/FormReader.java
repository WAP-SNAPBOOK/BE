package com.example.easybooking.form;

import com.example.easybooking.form.domain.Form;
import com.example.easybooking.form.domain.repository.FormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FormReader {
    private final FormRepository formRepository;

    public Form read(Long id){
        return formRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid form ID"));
    }

    public Form readDefaultForm(){
        return formRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Default form not found"));
    }

    public Form readByShopId(Long shopId){
        return formRepository.findByShopId(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid shop ID"));
    }
}
