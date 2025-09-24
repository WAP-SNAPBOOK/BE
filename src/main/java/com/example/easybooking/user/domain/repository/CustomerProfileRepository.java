package com.example.easybooking.user.domain.repository;

import com.example.easybooking.user.domain.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile,Long> {
}
