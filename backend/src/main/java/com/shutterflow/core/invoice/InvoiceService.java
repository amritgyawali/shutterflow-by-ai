package com.shutterflow.core.invoice;

import com.shutterflow.core.common.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository lineItemRepository;
    private final InvoiceSequenceRepository sequenceRepository;

    @Transactional
    public Invoice createInvoice(String studioId, String clientId, String bookingId, List<InvoiceLineItem> lineItems, BigDecimal taxRate, String currency) {
        String invoiceNumber = generateNextInvoiceNumber(studioId);

        BigDecimal subtotal = lineItems.stream()
                .map(InvoiceLineItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxAmount = subtotal.multiply(taxRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(taxAmount);

        Invoice invoice = Invoice.builder()
                .id(UUID.randomUUID().toString())
                .studioId(studioId)
                .clientId(clientId)
                .bookingId(bookingId)
                .invoiceNumber(invoiceNumber)
                .status(InvoiceStatus.DRAFT)
                .subtotal(subtotal)
                .taxRate(taxRate)
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .amountDue(totalAmount)
                .currency(currency != null ? currency : "AUD")
                .build();

        Invoice saved = invoiceRepository.save(invoice);

        // Save line items
        for (int i = 0; i < lineItems.size(); i++) {
            InvoiceLineItem item = lineItems.get(i);
            item.setId(UUID.randomUUID().toString());
            item.setInvoiceId(saved.getId());
            item.setSortOrder(i);
            lineItemRepository.save(item);
        }

        log.info("Created invoice {} for studio {}", invoiceNumber, studioId);
        return saved;
    }

    @Transactional
    public Invoice issueInvoice(String invoiceId, LocalDate dueDate) {
        Invoice invoice = getInvoice(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new AppException("Only DRAFT invoices can be issued", HttpStatus.BAD_REQUEST);
        }
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setDueDate(dueDate);
        invoice.setIssuedAt(LocalDateTime.now());
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice recordPayment(String invoiceId, BigDecimal amount) {
        Invoice invoice = getInvoice(invoiceId);

        BigDecimal newAmountPaid = invoice.getAmountPaid().add(amount);
        BigDecimal newAmountDue = invoice.getTotalAmount().subtract(newAmountPaid);

        invoice.setAmountPaid(newAmountPaid);
        invoice.setAmountDue(newAmountDue.max(BigDecimal.ZERO));

        if (newAmountDue.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(LocalDateTime.now());
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        log.info("Recorded payment of {} on invoice {}. New balance: {}", amount, invoiceId, invoice.getAmountDue());
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public void markOverdueInvoices() {
        List<Invoice> overdue = invoiceRepository.findOverdueInvoices(LocalDate.now());
        for (Invoice invoice : overdue) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoiceRepository.save(invoice);
            log.info("Invoice {} marked as OVERDUE", invoice.getInvoiceNumber());
        }
    }

    public Invoice getInvoice(String invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException("Invoice not found", HttpStatus.NOT_FOUND));
    }

    public List<Invoice> getStudioInvoices(String studioId) {
        return invoiceRepository.findByStudioId(studioId);
    }

    public List<Invoice> getClientInvoices(String clientId) {
        return invoiceRepository.findByClientId(clientId);
    }

    private String generateNextInvoiceNumber(String studioId) {
        InvoiceSequence sequence = sequenceRepository.findById(studioId)
                .orElseGet(() -> {
                    InvoiceSequence s = InvoiceSequence.builder()
                            .studioId(studioId)
                            .lastNumber(0)
                            .prefix("INV")
                            .build();
                    return sequenceRepository.save(s);
                });

        int nextNumber = sequence.getLastNumber() + 1;
        sequence.setLastNumber(nextNumber);
        sequenceRepository.save(sequence);

        return String.format("%s-%d-%03d", sequence.getPrefix(), Year.now().getValue(), nextNumber);
    }
}
