package com.example.easybooking.form.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.easybooking.form.domain.Form;
import com.example.easybooking.form.domain.FormField;

public interface FormFieldRepository extends JpaRepository<FormField, Long> {
    List<FormField> findByForm(Form form);
    @Query("SELECT DISTINCT f FROM FormField f LEFT JOIN FETCH f.options WHERE f.form = :form ORDER BY f.displayOrder")
    List<FormField> findByFormWithOptions(@Param("form") Form form);
}
