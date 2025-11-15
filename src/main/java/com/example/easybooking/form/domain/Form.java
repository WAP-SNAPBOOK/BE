package com.example.easybooking.form.domain;

import com.example.easybooking.shop.domain.Shop;
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


    public static Form createForm(Shop shop) {
        Form form = new Form();
        form.shopId = shop.getId();
        form.name = shop.getBusinessName() + " 기본 폼";
        return form;
    }

    public static Form createFormTemplate() {
        Form form = new Form();
        form.name = "시스템 기본 폼";
        return form;
    }

    public void updateName(String name) {
        this.name = name;
    }

}
