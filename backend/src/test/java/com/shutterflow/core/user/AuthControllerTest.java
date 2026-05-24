package com.shutterflow.core.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shutterflow.core.studio.Studio;
import com.shutterflow.core.studio.StudioRepository;
import com.shutterflow.core.user.dto.RegisterStudioRequest;
import com.shutterflow.core.user.dto.RegisterPhotographerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudioRepository studioRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
        studioRepository.deleteAll();
    }

    @Test
    void registerStudio_ShouldSucceed_WhenPayloadIsValid() throws Exception {
        RegisterStudioRequest request = RegisterStudioRequest.builder()
                .studioName("Silver Light Studios")
                .subdomain("silverlight")
                .ownerEmail("owner@silverlight.com")
                .ownerPassword("secure_password_123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register-studio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Studio registered successfully"))
                .andExpect(jsonPath("$.data.email").value("owner@silverlight.com"))
                .andExpect(jsonPath("$.data.role").value("STUDIO_OWNER"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.studioId").exists());
    }

    @Test
    void registerStudio_ShouldFail_WhenSubdomainIsDuplicate() throws Exception {
        // Save initial studio directly to trigger duplicate exception
        String studioId = UUID.randomUUID().toString();
        Studio existingStudio = Studio.builder()
                .id(studioId)
                .name("Old Studio")
                .subdomain("duplicate")
                .build();
        studioRepository.save(existingStudio);

        RegisterStudioRequest request = RegisterStudioRequest.builder()
                .studioName("Silver Light Studios")
                .subdomain("duplicate") // Duplicate Subdomain
                .ownerEmail("owner@silverlight.com")
                .ownerPassword("secure_password_123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register-studio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Subdomain is already registered by another studio"));
    }

    @Test
    void registerStudio_ShouldFail_WhenEmailIsInvalid() throws Exception {
        RegisterStudioRequest request = RegisterStudioRequest.builder()
                .studioName("Silver Light Studios")
                .subdomain("silverlight")
                .ownerEmail("invalid_email_format") // Invalid Email
                .ownerPassword("123") // Too Short
                .build();

        mockMvc.perform(post("/api/v1/auth/register-studio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.ownerEmail").value("Invalid email format"))
                .andExpect(jsonPath("$.data.ownerPassword").value("Password must be at least 8 characters long"));
    }

    @Test
    void registerPhotographer_ShouldSucceed_WhenStandaloneAndValid() throws Exception {
        RegisterPhotographerRequest request = RegisterPhotographerRequest.builder()
                .email("freelance@shutterflow.com")
                .password("photographer_pass_123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register-photographer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Photographer registered successfully"))
                .andExpect(jsonPath("$.data.email").value("freelance@shutterflow.com"))
                .andExpect(jsonPath("$.data.role").value("PHOTOGRAPHER"))
                .andExpect(jsonPath("$.data.studioId").isEmpty());
    }

    @Test
    void registerPhotographer_ShouldSucceed_WhenInvitedAndValid() throws Exception {
        RegisterPhotographerRequest request = RegisterPhotographerRequest.builder()
                .email("team_member@shutterflow.com")
                .password("team_member_pass_123")
                .inviteToken("valid-mock-token-123") // Valid Token
                .build();

        mockMvc.perform(post("/api/v1/auth/register-photographer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Photographer registered successfully"))
                .andExpect(jsonPath("$.data.email").value("team_member@shutterflow.com"))
                .andExpect(jsonPath("$.data.role").value("PHOTOGRAPHER"))
                .andExpect(jsonPath("$.data.studioId").value("mock-studio-uuid"));
    }

    @Test
    void registerPhotographer_ShouldFail_WhenInviteTokenIsInvalid() throws Exception {
        RegisterPhotographerRequest request = RegisterPhotographerRequest.builder()
                .email("intruder@shutterflow.com")
                .password("intruder_pass_123")
                .inviteToken("invalid-expired-token") // Invalid Token
                .build();

        mockMvc.perform(post("/api/v1/auth/register-photographer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired invitation token"));
    }
}
