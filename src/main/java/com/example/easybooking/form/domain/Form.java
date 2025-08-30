package com.example.easybooking.form.domain;

import jakarta.persistence.*;
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

}
