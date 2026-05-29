package com.shutterflow.core.contract;

import com.shutterflow.core.booking.Booking;
import com.shutterflow.core.booking.BookingRepository;
import com.shutterflow.core.client.Client;
import com.shutterflow.core.client.ClientRepository;
import com.shutterflow.core.common.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractTemplateRepository templateRepository;
    private final BookingRepository bookingRepository;
    private final ClientRepository clientRepository;

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(\\w+)}}");

    @Transactional
    public ContractTemplate createTemplate(String studioId, String name, String bodyHtml, String eventType) {
        ContractTemplate template = ContractTemplate.builder()
                .id(UUID.randomUUID().toString())
                .studioId(studioId)
                .name(name)
                .bodyHtml(bodyHtml)
                .eventType(eventType)
                .build();
        return templateRepository.save(template);
    }

    @Transactional
    public Contract createContractFromTemplate(String studioId, String templateId, String bookingId, String clientId) {
        ContractTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new AppException("Contract template not found", HttpStatus.NOT_FOUND));

        String compiledHtml = compileTemplate(template.getBodyHtml(), bookingId, clientId);

        Contract contract = Contract.builder()
                .id(UUID.randomUUID().toString())
                .studioId(studioId)
                .templateId(templateId)
                .bookingId(bookingId)
                .clientId(clientId)
                .status(ContractStatus.DRAFT)
                .compiledHtml(compiledHtml)
                .build();

        return contractRepository.save(contract);
    }

    @Transactional
    public Contract sendContract(String contractId) {
        Contract contract = getContract(contractId);
        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new AppException("Only DRAFT contracts can be sent", HttpStatus.BAD_REQUEST);
        }
        contract.setStatus(ContractStatus.SENT);
        return contractRepository.save(contract);
    }

    @Transactional
    public Contract clientSign(String contractId, String signatureData, String clientIp) {
        Contract contract = getContract(contractId);
        if (contract.getStatus() != ContractStatus.SENT) {
            throw new AppException("Contract must be in SENT status to sign", HttpStatus.BAD_REQUEST);
        }
        contract.setClientSignatureData(signatureData);
        contract.setClientSignedAt(LocalDateTime.now());
        contract.setClientSignedIp(clientIp);
        contract.setStatus(ContractStatus.SIGNED);
        return contractRepository.save(contract);
    }

    @Transactional
    public Contract photographerCountersign(String contractId, String signatureData, String ip) {
        Contract contract = getContract(contractId);
        if (contract.getStatus() != ContractStatus.SIGNED) {
            throw new AppException("Contract must be SIGNED by client before countersigning", HttpStatus.BAD_REQUEST);
        }
        contract.setPhotographerSignatureData(signatureData);
        contract.setPhotographerSignedAt(LocalDateTime.now());
        contract.setPhotographerSignedIp(ip);
        contract.setStatus(ContractStatus.COUNTERSIGNED);
        return contractRepository.save(contract);
    }

    public Contract getContract(String contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException("Contract not found", HttpStatus.NOT_FOUND));
    }

    public List<Contract> getStudioContracts(String studioId) {
        return contractRepository.findByStudioId(studioId);
    }

    public List<ContractTemplate> getStudioTemplates(String studioId) {
        return templateRepository.findByStudioId(studioId);
    }

    /**
     * Replace placeholders like {{client_name}}, {{event_date}}, etc. with actual values.
     */
    private String compileTemplate(String templateHtml, String bookingId, String clientId) {
        Client client = clientRepository.findById(clientId).orElse(null);
        Booking booking = bookingId != null ? bookingRepository.findById(bookingId).orElse(null) : null;

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(templateHtml);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = resolvePlaceholder(placeholder, client, booking);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String resolvePlaceholder(String placeholder, Client client, Booking booking) {
        return switch (placeholder) {
            case "client_name" -> client != null ? client.getFirstName() + " " + (client.getLastName() != null ? client.getLastName() : "") : "";
            case "client_email" -> client != null ? client.getEmail() : "";
            case "event_date" -> booking != null ? booking.getEventDate().toString() : "";
            case "event_location" -> booking != null && booking.getEventLocation() != null ? booking.getEventLocation() : "";
            case "booking_amount" -> booking != null ? booking.getTotalAmount().toPlainString() : "";
            case "event_type" -> booking != null && booking.getEventType() != null ? booking.getEventType() : "";
            default -> "{{" + placeholder + "}}";
        };
    }
}
