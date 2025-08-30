package com.example.easybooking.form.domain.repository;

import com.example.easybooking.form.domain.Form;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FormRepository extends JpaRepository<Form,Long> {
    Optional<Form> findByShopId(Long shopId);
}
