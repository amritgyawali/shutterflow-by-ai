package com.shutterflow.core.contract;

import com.shutterflow.core.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/studios/{studioId}/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @Data
    public static class CreateTemplateRequest {
        @NotBlank
        private String name;
        @NotBlank
        private String bodyHtml;
        private String eventType;
    }

    @Data
    public static class CreateContractRequest {
        @NotBlank
        private String templateId;
        @NotBlank
        private String clientId;
        private String bookingId;
    }

    @Data
    public static class SignRequest {
        @NotBlank
        private String signatureData;
    }

    // Template CRUD
    @PostMapping("/templates")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<ContractTemplate>> createTemplate(
            @PathVariable String studioId,
            @Valid @RequestBody CreateTemplateRequest request) {
        ContractTemplate template = contractService.createTemplate(
                studioId, request.getName(), request.getBodyHtml(), request.getEventType());
        return ResponseEntity.ok(ApiResponse.success(template, "Template created"));
    }

    @GetMapping("/templates")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<ContractTemplate>>> getTemplates(@PathVariable String studioId) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getStudioTemplates(studioId), "Fetched templates"));
    }

    // Contract lifecycle
    @PostMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Contract>> createContract(
            @PathVariable String studioId,
            @Valid @RequestBody CreateContractRequest request) {
        Contract contract = contractService.createContractFromTemplate(
                studioId, request.getTemplateId(), request.getBookingId(), request.getClientId());
        return ResponseEntity.ok(ApiResponse.success(contract, "Contract created"));
    }

    @GetMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<Contract>>> getContracts(@PathVariable String studioId) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getStudioContracts(studioId), "Fetched contracts"));
    }

    @GetMapping("/{contractId}")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Contract>> getContract(
            @PathVariable String studioId,
            @PathVariable String contractId) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getContract(contractId), "Fetched contract"));
    }

    @PostMapping("/{contractId}/send")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Contract>> sendContract(
            @PathVariable String studioId,
            @PathVariable String contractId) {
        Contract sent = contractService.sendContract(contractId);
        return ResponseEntity.ok(ApiResponse.success(sent, "Contract sent to client"));
    }

    @PostMapping("/{contractId}/sign")
    public ResponseEntity<ApiResponse<Contract>> clientSign(
            @PathVariable String studioId,
            @PathVariable String contractId,
            @Valid @RequestBody SignRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        Contract signed = contractService.clientSign(contractId, request.getSignatureData(), clientIp);
        return ResponseEntity.ok(ApiResponse.success(signed, "Contract signed"));
    }

    @PostMapping("/{contractId}/countersign")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Contract>> countersign(
            @PathVariable String studioId,
            @PathVariable String contractId,
            @Valid @RequestBody SignRequest request,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Contract countersigned = contractService.photographerCountersign(contractId, request.getSignatureData(), ip);
        return ResponseEntity.ok(ApiResponse.success(countersigned, "Contract countersigned"));
    }
}
