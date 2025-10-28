package com.example.easybooking.form.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Form {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long shopId;

    private String name;

    private String title;

    public static Form createForm(Form defaultForm, Long shopId) {
        Form form = new Form();
        form.shopId = defaultForm.getShopId();
        form.name = defaultForm.getName();
        form.title = defaultForm.getTitle();
        return form;
    }

    public static Form createFormTemplate() {
        Form form = new Form();
        form.shopId = 0L;
        form.name = "시스템 기본 폼";
        form.title = "기본 설정 템플릿";

        return form;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

}
