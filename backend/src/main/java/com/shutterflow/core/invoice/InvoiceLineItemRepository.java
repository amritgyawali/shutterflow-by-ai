package com.shutterflow.core.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, String> {
    List<InvoiceLineItem> findByInvoiceIdOrderBySortOrder(String invoiceId);
}
