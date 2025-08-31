package com.example.easybooking.form;

import com.example.easybooking.form.domain.Form;
import com.example.easybooking.form.domain.FormCopyService;
import com.example.easybooking.form.domain.FormField;
import com.example.easybooking.form.dto.FormResponse;
import com.example.easybooking.form.mapper.FormMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FormService {

    private final FormReader formReader;
    private final FormMapper formMapper;
    private final FormCopyService formCopyService;

    public FormResponse getForm(Long shopId){
        Form form = formReader.readByShopId(shopId);
        return formMapper.toDto(form);
    }

    public void createDefaultForm(Long shopId) {
        formCopyService.copyDefaultFormToShop(shopId);
    }

}
