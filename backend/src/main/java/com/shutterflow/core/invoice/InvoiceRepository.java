package com.shutterflow.core.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    List<Invoice> findByStudioId(String studioId);

    List<Invoice> findByClientId(String clientId);

    List<Invoice> findByBookingId(String bookingId);

    Optional<Invoice> findByInvoiceNumberAndStudioId(String invoiceNumber, String studioId);

    @Query("SELECT i FROM Invoice i WHERE i.status = 'SENT' AND i.dueDate < :today")
    List<Invoice> findOverdueInvoices(LocalDate today);

    @Query("SELECT i FROM Invoice i WHERE i.status = 'SENT' AND i.dueDate = :dueDate")
    List<Invoice> findInvoicesDueOn(LocalDate dueDate);

    @Query("SELECT i FROM Invoice i WHERE i.studioId = :studioId AND i.status = :status")
    List<Invoice> findByStudioIdAndStatus(String studioId, InvoiceStatus status);
}
