package com.shutterflow.core.commission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, String> {

    List<Commission> findByStudioId(String studioId);

    List<Commission> findByPhotographerIdAndStatus(String photographerId, String status);

    List<Commission> findByPhotographerId(String photographerId);

    @Query("SELECT SUM(c.commissionAmount) FROM Commission c WHERE c.photographerId = :photographerId AND c.status = 'PENDING'")
    BigDecimal getTotalPendingCommission(String photographerId);

    @Query("SELECT SUM(c.commissionAmount) FROM Commission c WHERE c.photographerId = :photographerId AND c.status = 'PAID'")
    BigDecimal getTotalPaidCommission(String photographerId);
}
