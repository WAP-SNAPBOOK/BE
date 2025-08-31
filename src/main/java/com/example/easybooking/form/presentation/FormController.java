package com.example.easybooking.form.presentation;

import org.springframework.web.bind.annotation.*;

import com.example.easybooking.form.FormService;
import com.example.easybooking.form.dto.FormResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/form")
@Slf4j
public class FormController {

    private final FormService formService;

    @GetMapping("/{shopId}")
    public FormResponse getForm(@PathVariable Long shopId){
        FormResponse formResponse = formService.getForm(shopId);
        return formResponse;
    }
}
