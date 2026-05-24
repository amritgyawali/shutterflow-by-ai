package com.shutterflow.core.studio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shutterflow.core.common.ApiResponse;
import com.shutterflow.core.user.User;
import com.shutterflow.core.user.UserRepository;
import com.shutterflow.core.user.UserRole;
import com.shutterflow.core.user.PhotographerProfile;
import com.shutterflow.core.user.PhotographerProfileRepository;
import com.shutterflow.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class Sprint3IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private StudioRepository studioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudioInvitationRepository invitationRepository;

    @Autowired
    private PhotographerProfileRepository photographerProfileRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.shutterflow.infrastructure.mail.EmailService emailService;

    private String studioIdA;
    private String studioIdB;
    private String ownerTokenA;
    private String ownerTokenB;
    private String photographerTokenA;

    @BeforeEach
    void setUp() {
        studioIdA = UUID.randomUUID().toString();
        studioIdB = UUID.randomUUID().toString();

        Studio studioA = Studio.builder().id(studioIdA).name("Studio Alpha").subdomain("alpha").planTier(PlanTier.STARTER).build();
        Studio studioB = Studio.builder().id(studioIdB).name("Studio Beta").subdomain("beta").planTier(PlanTier.STARTER).build();
        studioRepository.save(studioA);
        studioRepository.save(studioB);

        ownerTokenA = "Bearer " + jwtTokenProvider.generateAccessToken("owner@alpha.com", UserRole.STUDIO_OWNER, studioIdA);
        ownerTokenB = "Bearer " + jwtTokenProvider.generateAccessToken("owner@beta.com", UserRole.STUDIO_OWNER, studioIdB);
        photographerTokenA = "Bearer " + jwtTokenProvider.generateAccessToken("photo@alpha.com", UserRole.PHOTOGRAPHER, studioIdA);
    }

    @Test
    void getBranding_ShouldSucceed_AndReturnDefaults() throws Exception {
        mockMvc.perform(get("/api/v1/studios/" + studioIdA + "/branding"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.primaryColor").value("#1f2937"))
                .andExpect(jsonPath("$.data.secondaryColor").value("#10b981"))
                .andExpect(jsonPath("$.data.customFont").value("Outfit"));
    }

    @Test
    void updateBranding_ShouldSucceed_WhenOwnerAuthorizes() throws Exception {
        mockMvc.perform(patch("/api/v1/studios/" + studioIdA + "/branding")
                        .header("Authorization", ownerTokenA)
                        .param("primaryColor", "#ff0000")
                        .param("secondaryColor", "#00ff00")
                        .param("customFont", "Roboto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.primaryColor").value("#ff0000"))
                .andExpect(jsonPath("$.data.secondaryColor").value("#00ff00"))
                .andExpect(jsonPath("$.data.customFont").value("Roboto"));
    }

    @Test
    void registerPhotographer_ShouldFail_WhenQuotaIsBreachedForStarterPlan() throws Exception {
        // Starters can only have 1 photographer (owner does not count towards photographer limit, but we count the registered team photographers)
        // Add 1 photographer to Studio Alpha
        User photo1 = User.builder()
                .id(UUID.randomUUID().toString())
                .email("photo1@alpha.com")
                .passwordHash("hashed")
                .role(UserRole.PHOTOGRAPHER)
                .studioId(studioIdA)
                .build();
        userRepository.save(photo1);

        // Attempting to invite or register another photographer should throw 403 plan quota limit exception
        mockMvc.perform(post("/api/v1/studios/" + studioIdA + "/invite")
                        .header("Authorization", ownerTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"photo2@alpha.com\",\"role\":\"PHOTOGRAPHER\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("reached the photographer limit")));
    }

    @Test
    void inviteAndAcceptFlow_ShouldSucceed() throws Exception {
        // Invite a photographer
        mockMvc.perform(post("/api/v1/studios/" + studioIdB + "/invite")
                        .header("Authorization", ownerTokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invitee@beta.com\",\"role\":\"PHOTOGRAPHER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Invitation sent successfully"));

        // Retrieve invitation from database
        StudioInvitation invitation = invitationRepository.findAll().stream()
                .filter(i -> i.getEmail().equals("invitee@beta.com"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Invitation not found in database"));

        // Accept invitation
        String acceptPayload = String.format("{\"token\":\"%s\",\"password\":\"newpassword123\"}", invitation.getToken());

        mockMvc.perform(post("/api/v1/auth/accept-invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(acceptPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("invitee@beta.com"))
                .andExpect(jsonPath("$.data.role").value("PHOTOGRAPHER"))
                .andExpect(jsonPath("$.data.studioId").value(studioIdB));

        // Confirm invitation is now marked as redeemed
        StudioInvitation updatedInvite = invitationRepository.findById(invitation.getToken()).orElseThrow();
        assertTrue(updatedInvite.isRedeemed());
    }

    @Test
    void teamDashboards_AndProfilePatches_ShouldSucceed() throws Exception {
        User photographer = User.builder()
                .id(UUID.randomUUID().toString())
                .email("photo_member@alpha.com")
                .passwordHash("hashed")
                .role(UserRole.PHOTOGRAPHER)
                .studioId(studioIdA)
                .build();
        userRepository.save(photographer);

        // Fetch team list
        mockMvc.perform(get("/api/v1/studios/" + studioIdA + "/photographers")
                        .header("Authorization", photographerTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].email").value("photo_member@alpha.com"));

        // Update photographer profile availability and bio
        mockMvc.perform(patch("/api/v1/studios/" + studioIdA + "/photographers/" + photographer.getId() + "/profile")
                        .header("Authorization", photographerTokenA)
                        .param("bio", "Expert portrait photographer")
                        .param("specializations", "Portraits, Headshots")
                        .param("availabilityHours", "{\"monday\":[\"10:00-16:00\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bio").value("Expert portrait photographer"))
                .andExpect(jsonPath("$.data.specializations").value("Portraits, Headshots"))
                .andExpect(jsonPath("$.data.availabilityHours").value("{\"monday\":[\"10:00-16:00\"]}"));
    }
}
