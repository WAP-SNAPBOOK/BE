package com.example.easybooking.form.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "form_field_options")
public class FormFieldOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String value;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private Integer displayOrder = 0;

    @ManyToOne
    @JoinColumn(name = "form_field_id")
    private FormField formField;
}