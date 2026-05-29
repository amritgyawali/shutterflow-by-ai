package com.shutterflow.core.invoice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled jobs for invoice management:
 * - Mark overdue invoices daily
 * - Send payment reminders 3 days before due date
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceScheduledJobs {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;

    /**
     * Runs daily at midnight - marks SENT invoices past due date as OVERDUE.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void markOverdueInvoices() {
        log.info("Running scheduled job: mark overdue invoices");
        invoiceService.markOverdueInvoices();
    }

    /**
     * Runs daily at 9am - sends payment reminders for invoices due in 3 days.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendPaymentReminders() {
        LocalDate threeDaysFromNow = LocalDate.now().plusDays(3);
        List<Invoice> invoicesDueSoon = invoiceRepository.findInvoicesDueOn(threeDaysFromNow);

        for (Invoice invoice : invoicesDueSoon) {
            log.info("Payment reminder: Invoice {} due on {}", invoice.getInvoiceNumber(), invoice.getDueDate());
            // In production: dispatch email via EmailService
        }

        log.info("Payment reminders sent for {} invoices", invoicesDueSoon.size());
    }
}
