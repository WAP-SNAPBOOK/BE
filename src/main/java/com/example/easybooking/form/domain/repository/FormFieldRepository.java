package com.example.easybooking.form.domain.repository;

import com.example.easybooking.form.domain.Form;
import com.example.easybooking.form.domain.FormField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormFieldRepository extends JpaRepository<FormField, Long> {
    List<FormField> findByForm(Form form);
}
