package com.shutterflow.core.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shutterflow.core.common.ApiResponse;
import com.shutterflow.core.client.event.PaymentCompletedEvent;
import com.shutterflow.infrastructure.security.JwtTokenProvider;
import com.shutterflow.core.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class Sprint4IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientContactRepository clientContactRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private String studioId;
    private String otherStudioId;
    private String token;
    private String otherToken;

    @BeforeEach
    void setUp() {
        studioId = UUID.randomUUID().toString();
        otherStudioId = UUID.randomUUID().toString();

        token = "Bearer " + jwtTokenProvider.generateAccessToken("owner@studio.com", UserRole.STUDIO_OWNER, studioId);
        otherToken = "Bearer " + jwtTokenProvider.generateAccessToken("owner@other.com", UserRole.STUDIO_OWNER, otherStudioId);
    }

    @Test
    void crmCrudOperations_ShouldSucceed() throws Exception {
        String payload = "{\"firstName\":\"Amrit\",\"lastName\":\"Gyawali\",\"email\":\"amrit@shutterflow.com\",\"leadSource\":\"Instagram\",\"tags\":\"VIP,Referral\",\"notes\":\"Corporate client\"}";

        // 1. Create Client
        mockMvc.perform(post("/api/v1/studios/" + studioId + "/clients")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Amrit"))
                .andExpect(jsonPath("$.data.email").value("amrit@shutterflow.com"))
                .andExpect(jsonPath("$.data.tags").value("VIP,Referral"));

        Client client = clientRepository.findAll().stream()
                .filter(c -> c.getEmail().equals("amrit@shutterflow.com"))
                .findFirst()
                .orElseThrow();

        // 2. Fetch Client
        mockMvc.perform(get("/api/v1/studios/" + studioId + "/clients/" + client.getId())
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Amrit"));

        // 3. Update Client
        String updatePayload = "{\"firstName\":\"Amrit Kumar\",\"email\":\"amrit@shutterflow.com\"}";
        mockMvc.perform(put("/api/v1/studios/" + studioId + "/clients/" + client.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Amrit Kumar"));

        // 4. Delete Client
        mockMvc.perform(delete("/api/v1/studios/" + studioId + "/clients/" + client.getId())
                        .header("Authorization", token))
                .andExpect(status().isOk());

        assertFalse(clientRepository.findById(client.getId()).isPresent());
    }

    @Test
    void secondaryContacts_AndCascadeDeletes_ShouldSucceed() throws Exception {
        Client client = Client.builder()
                .id(UUID.randomUUID().toString())
                .firstName("John")
                .email("john@example.com")
                .studioId(studioId)
                .build();
        clientRepository.save(client);

        // Add contact
        String contactPayload = "{\"name\":\"Jane Doe\",\"email\":\"jane@example.com\",\"phone\":\"0400000000\",\"relation\":\"Bride\"}";
        mockMvc.perform(post("/api/v1/studios/" + studioId + "/clients/" + client.getId() + "/contacts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contactPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Jane Doe"))
                .andExpect(jsonPath("$.data.relation").value("Bride"));

        // Fetch client and verify contacts array populated
        mockMvc.perform(get("/api/v1/studios/" + studioId + "/clients/" + client.getId())
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contacts[0].name").value("Jane Doe"));

        // Delete client and verify cascade deletion
        clientRepository.delete(client);
        assertTrue(clientContactRepository.findByClientId(client.getId()).isEmpty());
    }

    @Test
    void portalTokenProvisioning_ShouldGenerateAndRevoke() throws Exception {
        Client client = Client.builder()
                .id(UUID.randomUUID().toString())
                .firstName("Steve")
                .email("steve@example.com")
                .studioId(studioId)
                .build();
        clientRepository.save(client);

        // Generate token
        mockMvc.perform(post("/api/v1/studios/" + studioId + "/clients/" + client.getId() + "/portal-token")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.portalToken").exists());

        // Revoke token
        mockMvc.perform(delete("/api/v1/studios/" + studioId + "/clients/" + client.getId() + "/portal-token")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.portalToken").isEmpty());
    }

    @Test
    void leadRoiAnalytics_ShouldCompileCorrectly() throws Exception {
        Client c1 = Client.builder().id(UUID.randomUUID().toString()).firstName("A").email("a@a.com").studioId(studioId).leadSource("Instagram").totalSpend(new BigDecimal("1200.00")).build();
        Client c2 = Client.builder().id(UUID.randomUUID().toString()).firstName("B").email("b@b.com").studioId(studioId).leadSource("Instagram").totalSpend(new BigDecimal("800.00")).build();
        Client c3 = Client.builder().id(UUID.randomUUID().toString()).firstName("C").email("c@c.com").studioId(studioId).leadSource("Google Search").totalSpend(new BigDecimal("1500.00")).build();
        clientRepository.save(c1);
        clientRepository.save(c2);
        clientRepository.save(c3);

        mockMvc.perform(get("/api/v1/studios/" + studioId + "/crm/leads/roi")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallRevenue").value(3500.00))
                .andExpect(jsonPath("$.data.leadMetrics").isArray());
    }

    @Test
    void csvBulkImporter_ShouldProcessRowsInParallel() throws Exception {
        String csvContent = "First Name,Last Name,Email,Lead Source,Tags,Notes\n" +
                "Jane,Doe,jane@csv.com,Instagram,VIP,Imported client\n" +
                "John,Smith,john@csv.com,Google Search,Corporate,Imported too\n" +
                ",NoName,badrow@csv.com,Facebook,,Bad first name\n" +
                "Duplicate,Mail,john@csv.com,Facebook,,Duplicate email\n";

        MockMultipartFile csvFile = new MockMultipartFile(
                "file", "import.csv", "text/csv", csvContent.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/studios/" + studioId + "/clients/import")
                        .file(csvFile)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.importedCount").value(2)) // Jane and John Smith successfully imported
                .andExpect(jsonPath("$.data.skippedCount").value(1)) // Duplicate skipped
                .andExpect(jsonPath("$.data.errors").isArray());
    }

    @Test
    void eventDrivenSpendRecalculation_ShouldUpdateTotalSpend() {
        Client client = Client.builder()
                .id(UUID.randomUUID().toString())
                .firstName("David")
                .email("david@example.com")
                .studioId(studioId)
                .totalSpend(BigDecimal.ZERO)
                .build();
        clientRepository.save(client);

        // Publish Payment Completed Event
        eventPublisher.publishEvent(new PaymentCompletedEvent(this, client.getId(), new BigDecimal("450.00")));

        // Verify aggregator updated the spend in DB
        Client updatedClient = clientRepository.findById(client.getId()).orElseThrow();
        assertEquals(0, updatedClient.getTotalSpend().compareTo(new BigDecimal("450.00")));
    }
}
