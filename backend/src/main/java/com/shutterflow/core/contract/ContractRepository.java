package com.shutterflow.core.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, String> {
    List<Contract> findByStudioId(String studioId);
    List<Contract> findByClientId(String clientId);
    List<Contract> findByBookingId(String bookingId);
    List<Contract> findByStudioIdAndStatus(String studioId, ContractStatus status);
}
