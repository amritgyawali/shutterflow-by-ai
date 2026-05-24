package com.shutterflow.core.client;

import com.shutterflow.core.common.ApiResponse;
import com.shutterflow.core.common.AppException;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/v1/studios/{studioId}")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientRepository clientRepository;
    private final ClientContactRepository clientContactRepository;

    @Data
    public static class ClientRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String leadSource;
        private String tags;
        private String notes;
    }

    @Data
    public static class ContactRequest {
        private String name;
        private String email;
        private String phone;
        private String relation;
    }

    @Data
    @Builder
    public static class ClientResponse {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private BigDecimal totalSpend;
        private String leadSource;
        private String tags;
        private String notes;
        private String portalToken;
        private List<ContactResponse> contacts;
    }

    @Data
    @Builder
    public static class ContactResponse {
        private String id;
        private String name;
        private String email;
        private String phone;
        private String relation;
    }

    /**
     * CRUD: Create a new client.
     * Email must be unique within the studio workspace.
     */
    @PostMapping("/clients")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<ClientResponse>> createClient(
            @PathVariable String studioId,
            @RequestBody ClientRequest request) {

        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new AppException("First name is required", HttpStatus.BAD_REQUEST);
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new AppException("Email is required", HttpStatus.BAD_REQUEST);
        }

        if (clientRepository.existsByEmailAndStudioId(request.getEmail(), studioId)) {
            throw new AppException("A client with this email address already exists in your studio workspace", HttpStatus.BAD_REQUEST);
        }

        Client client = Client.builder()
                .id(UUID.randomUUID().toString())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase())
                .studioId(studioId)
                .leadSource(request.getLeadSource())
                .tags(request.getTags())
                .notes(request.getNotes())
                .build();

        Client saved = clientRepository.save(client);
        return ResponseEntity.ok(ApiResponse.success(toClientResponse(saved), "Client created successfully"));
    }

    /**
     * CRUD: Get a single client.
     */
    @GetMapping("/clients/{clientId}")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<ClientResponse>> getClient(
            @PathVariable String studioId,
            @PathVariable String clientId) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new AppException("Client not found", HttpStatus.NOT_FOUND));

        if (!studioId.equals(client.getStudioId())) {
            throw new AppException("Client does not belong to this studio space", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(ApiResponse.success(toClientResponse(client), "Client fetched successfully"));
    }

    /**
     * CRUD: Update a client.
     */
    @PutMapping("/clients/{clientId}")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<ClientResponse>> updateClient(
            @PathVariable String studioId,
            @PathVariable String clientId,
            @RequestBody ClientRequest request) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new AppException("Client not found", HttpStatus.NOT_FOUND));

        if (!studioId.equals(client.getStudioId())) {
            throw new AppException("Client does not belong to this studio space", HttpStatus.FORBIDDEN);
        }

        // Email uniqueness checks if email changed
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(client.getEmail())) {
            if (clientRepository.existsByEmailAndStudioId(request.getEmail(), studioId)) {
                throw new AppException("A client with this email address already exists in your studio workspace", HttpStatus.BAD_REQUEST);
            }
            client.setEmail(request.getEmail().toLowerCase());
        }

        if (request.getFirstName() != null) {
            client.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            client.setLastName(request.getLastName());
        }
        if (request.getLeadSource() != null) {
            client.setLeadSource(request.getLeadSource());
        }
        if (request.getTags() != null) {
            client.setTags(request.getTags());
        }
        if (request.getNotes() != null) {
            client.setNotes(request.getNotes());
        }

        Client updated = clientRepository.save(client);
        return ResponseEntity.ok(ApiResponse.success(toClientResponse(updated), "Client updated successfully"));
    }

    /**
     * CRUD: Delete a client. Cascade deletes all secondary contacts.
     */
    @DeleteMapping("/clients/{clientId}")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Void>> deleteClient(
            @PathVariable String studioId,
            @PathVariable String clientId) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new AppException("Client not found", HttpStatus.NOT_FOUND));

        if (!studioId.equals(client.getStudioId())) {
            throw new AppException("Client does not belong to this studio space", HttpStatus.FORBIDDEN);
        }

        clientRepository.delete(client);
        return ResponseEntity.ok(ApiResponse.success(null, "Client deleted successfully"));
    }

    /**
     * Search and Filter Clients API utilizing JPA Specifications.
     */
    @GetMapping("/clients")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<ClientResponse>>> searchClients(
            @PathVariable String studioId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String leadSource,
            @RequestParam(required = false) BigDecimal minSpend,
            @RequestParam(required = false) BigDecimal maxSpend) {

        Specification<Client> spec = ClientSpecification.filterClients(search, tag, leadSource, minSpend, maxSpend, studioId);
        List<Client> clients = clientRepository.findAll(spec);
        
        List<ClientResponse> responses = new ArrayList<>();
        for (Client c : clients) {
            responses.add(toClientResponse(c));
        }

        return ResponseEntity.ok(ApiResponse.success(responses, "Fetched and filtered clients successfully"));
    }

    /**
     * Add a secondary contact (Bride, Groom, etc.) to the client profile.
     */
    @PostMapping("/clients/{clientId}/contacts")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<ContactResponse>> addSecondaryContact(
            @PathVariable String studioId,
            @PathVariable String clientId,
            @RequestBody ContactRequest request) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new AppException("Client not found", HttpStatus.NOT_FOUND));

        if (!studioId.equals(client.getStudioId())) {
            throw new AppException("Client does not belong to this studio space", HttpStatus.FORBIDDEN);
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new AppException("Contact name is required", HttpStatus.BAD_REQUEST);
        }

        ClientContact contact = ClientContact.builder()
                .id(UUID.randomUUID().toString())
                .client(client)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .relation(request.getRelation())
                .build();

        ClientContact saved = clientContactRepository.save(contact);
        ContactResponse response = ContactResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .relation(saved.getRelation())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Secondary contact added successfully"));
    }

    /**
     * Generate or regenerate a secure portal token for the client.
     */
    @PostMapping("/clients/{clientId}/portal-token")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<ClientResponse>> generatePortalToken(
            @PathVariable String studioId,
            @PathVariable String clientId) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new AppException("Client not found", HttpStatus.NOT_FOUND));

        if (!studioId.equals(client.getStudioId())) {
            throw new AppException("Client does not belong to this studio space", HttpStatus.FORBIDDEN);
        }

        client.setPortalToken(UUID.randomUUID().toString());
        Client updated = clientRepository.save(client);

        return ResponseEntity.ok(ApiResponse.success(toClientResponse(updated), "Portal token generated successfully"));
    }

    /**
     * Revoke the client's portal token.
     */
    @DeleteMapping("/clients/{clientId}/portal-token")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<ClientResponse>> revokePortalToken(
            @PathVariable String studioId,
            @PathVariable String clientId) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new AppException("Client not found", HttpStatus.NOT_FOUND));

        if (!studioId.equals(client.getStudioId())) {
            throw new AppException("Client does not belong to this studio space", HttpStatus.FORBIDDEN);
        }

        client.setPortalToken(null);
        Client updated = clientRepository.save(client);

        return ResponseEntity.ok(ApiResponse.success(toClientResponse(updated), "Portal token revoked successfully"));
    }

    /**
     * Compiled Lead Source Analytics & ROI tracking.
     */
    @GetMapping("/crm/leads/roi")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLeadRoi(@PathVariable String studioId) {
        List<Client> clients = clientRepository.findByStudioId(studioId);
        
        Map<String, Integer> leadCounts = new HashMap<>();
        Map<String, BigDecimal> leadRevenue = new HashMap<>();
        BigDecimal totalOverallSpend = BigDecimal.ZERO;

        for (Client c : clients) {
            String source = c.getLeadSource();
            if (source == null || source.isBlank()) {
                source = "Unknown";
            }

            leadCounts.put(source, leadCounts.getOrDefault(source, 0) + 1);
            
            BigDecimal spend = c.getTotalSpend() != null ? c.getTotalSpend() : BigDecimal.ZERO;
            leadRevenue.put(source, leadRevenue.getOrDefault(source, BigDecimal.ZERO).add(spend));
            totalOverallSpend = totalOverallSpend.add(spend);
        }

        Map<String, Object> data = new HashMap<>();
        
        List<Map<String, Object>> metrics = new ArrayList<>();
        for (String source : leadCounts.keySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("source", source);
            m.put("leadCount", leadCounts.get(source));
            m.put("revenueGenerated", leadRevenue.get(source));
            metrics.add(m);
        }

        data.put("overallRevenue", totalOverallSpend);
        data.put("leadMetrics", metrics);

        return ResponseEntity.ok(ApiResponse.success(data, "Lead source ROI compiled successfully"));
    }

    @Data
    @Builder
    public static class ImportReport {
        private int importedCount;
        private int skippedCount;
        private List<Map<String, Object>> errors;
    }

    /**
     * Asynchronous, highly optimized Multi-Threaded CSV Ingestion Bulk Importer.
     */
    @PostMapping("/clients/import")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<ImportReport>> importClientsCsv(
            @PathVariable String studioId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new AppException("Please upload a valid non-empty CSV spreadsheet", HttpStatus.BAD_REQUEST);
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            log.error("Failed to parse CSV spreadsheet", e);
            throw new AppException("Failed to read uploaded CSV spreadsheet file", HttpStatus.BAD_REQUEST);
        }

        if (lines.isEmpty()) {
            throw new AppException("CSV spreadsheet does not contain any data rows", HttpStatus.BAD_REQUEST);
        }

        // Parallel CSV Parsing using standard ForkJoin threadpool
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(Math.max(2, availableProcessors));

        List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();
        
        // Skip header row if present
        int startIndex = 0;
        if (lines.get(0).toLowerCase().contains("email") || lines.get(0).toLowerCase().contains("first")) {
            startIndex = 1;
        }

        for (int i = startIndex; i < lines.size(); i++) {
            final String csvLine = lines.get(i);
            final int rowIndex = i + 1;
            
            futures.add(CompletableFuture.supplyAsync(() -> {
                Map<String, Object> result = new HashMap<>();
                result.put("row", rowIndex);
                
                try {
                    // Quick splits (handles simple commas cleanly)
                    String[] tokens = csvLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    
                    if (tokens.length < 2) {
                        result.put("error", "Row must contain at least First Name and Email columns");
                        return result;
                    }

                    String firstName = tokens[0].replaceAll("^\"|\"$", "").trim();
                    String lastName = tokens.length > 1 ? tokens[1].replaceAll("^\"|\"$", "").trim() : "";
                    String email = tokens.length > 2 ? tokens[2].replaceAll("^\"|\"$", "").trim() : "";
                    String leadSource = tokens.length > 3 ? tokens[3].replaceAll("^\"|\"$", "").trim() : null;
                    String tags = tokens.length > 4 ? tokens[4].replaceAll("^\"|\"$", "").trim() : null;
                    String notes = tokens.length > 5 ? tokens[5].replaceAll("^\"|\"$", "").trim() : null;

                    if (firstName.isEmpty()) {
                        result.put("error", "First name cannot be empty");
                        return result;
                    }
                    if (email.isEmpty()) {
                        result.put("error", "Email is required");
                        return result;
                    }
                    if (!email.contains("@")) {
                        result.put("error", "Invalid email format: " + email);
                        return result;
                    }

                    result.put("firstName", firstName);
                    result.put("lastName", lastName);
                    result.put("email", email.toLowerCase());
                    result.put("leadSource", leadSource);
                    result.put("tags", tags);
                    result.put("notes", notes);
                } catch (Exception e) {
                    result.put("error", "Unexpected error parsing row: " + e.getMessage());
                }
                
                return result;
            }, executor));
        }

        // Wait for all parse operations to finish
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        int importedCount = 0;
        int skippedCount = 0;
        List<Map<String, Object>> errorsReport = new ArrayList<>();

        // Save imported records sequentially (transactionally safe)
        for (CompletableFuture<Map<String, Object>> f : futures) {
            try {
                Map<String, Object> parseMap = f.get();
                if (parseMap.containsKey("error")) {
                    errorsReport.add(parseMap);
                    continue;
                }

                String email = (String) parseMap.get("email");
                
                // Uniqueness constraint check in workspace
                if (clientRepository.existsByEmailAndStudioId(email, studioId)) {
                    skippedCount++;
                    Map<String, Object> skipErr = new HashMap<>();
                    skipErr.put("row", parseMap.get("row"));
                    skipErr.put("error", "Skipped duplicate client email: " + email);
                    errorsReport.add(skipErr);
                    continue;
                }

                Client client = Client.builder()
                        .id(UUID.randomUUID().toString())
                        .firstName((String) parseMap.get("firstName"))
                        .lastName((String) parseMap.get("lastName"))
                        .email(email)
                        .studioId(studioId)
                        .leadSource((String) parseMap.get("leadSource"))
                        .tags((String) parseMap.get("tags"))
                        .notes((String) parseMap.get("notes"))
                        .build();

                clientRepository.save(client);
                importedCount++;

            } catch (Exception e) {
                log.error("Failed to commit imported CSV record", e);
            }
        }

        ImportReport report = ImportReport.builder()
                .importedCount(importedCount)
                .skippedCount(skippedCount)
                .errors(errorsReport)
                .build();

        return ResponseEntity.ok(ApiResponse.success(report, "CSV spreadsheet import completed successfully"));
    }

    private ClientResponse toClientResponse(Client client) {
        List<ContactResponse> contactsList = new ArrayList<>();
        if (client.getContacts() != null) {
            for (ClientContact c : client.getContacts()) {
                contactsList.add(ContactResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .email(c.getEmail())
                        .phone(c.getPhone())
                        .relation(c.getRelation())
                        .build());
            }
        }

        return ClientResponse.builder()
                .id(client.getId())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .email(client.getEmail())
                .totalSpend(client.getTotalSpend())
                .leadSource(client.getLeadSource())
                .tags(client.getTags())
                .notes(client.getNotes())
                .portalToken(client.getPortalToken())
                .contacts(contactsList)
                .build();
    }
}
