package com.example.easybooking.form.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.easybooking.form.domain.Form;
import com.example.easybooking.form.domain.FormField;

public interface FormFieldRepository extends JpaRepository<FormField, Long> {
    List<FormField> findByForm(Form form);
    @Query("SELECT DISTINCT f FROM FormField f LEFT JOIN FETCH f.options WHERE f.form = :form ORDER BY f.displayOrder")
    List<FormField> findByFormWithOptions(@Param("form") Form form);

    @Query("SELECT f FROM FormField f WHERE f.form = :form AND f.displayOrder >= :fromOrder ORDER BY f.displayOrder")
    List<FormField> findFieldsFromOrder(@Param("form") Form form, @Param("fromOrder") Integer fromOrder);

    @Query("SELECT f FROM FormField f WHERE f.form = :form AND f.displayOrder BETWEEN :startOrder AND :endOrder ORDER BY f.displayOrder")
    List<FormField> findFieldsInRange(@Param("form") Form form, @Param("startOrder") Integer startOrder, @Param("endOrder") Integer endOrder);

    Optional<FormField> findByFormAndFieldId(Form form, String fieldId);
}
