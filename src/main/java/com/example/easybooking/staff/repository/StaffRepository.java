package com.example.easybooking.staff.repository;

import com.example.easybooking.staff.domain.Staff;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findByShopId(Long shopId);

    List<Staff> findByShopIdOrderByIdAsc(Long shopId);

    Optional<Staff> findByShopIdAndName(Long shopId, String name);

    Optional<Staff> findFirstByShopIdOrderByIdAsc(Long shopId);
}
