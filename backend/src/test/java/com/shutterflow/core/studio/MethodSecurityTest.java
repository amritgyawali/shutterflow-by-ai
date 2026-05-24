package com.shutterflow.core.studio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shutterflow.core.user.UserRole;
import com.shutterflow.infrastructure.security.JwtTokenProvider;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MethodSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private StudioRepository studioRepository;

    @Autowired
    private StudioSettingsRepository settingsRepository;

    private String validStudioId;
    private String otherStudioId;
    private String validStudioOwnerToken;
    private String otherStudioOwnerToken;
    private String validStudioPhotographerToken;

    @BeforeEach
    void setUp() {
        validStudioId = UUID.randomUUID().toString();
        otherStudioId = UUID.randomUUID().toString();

        Studio studio1 = Studio.builder().id(validStudioId).name("Valid Studio").subdomain("valid").build();
        Studio studio2 = Studio.builder().id(otherStudioId).name("Other Studio").subdomain("other").build();
        studioRepository.save(studio1);
        studioRepository.save(studio2);

        StudioSettings settings = StudioSettings.builder().studioId(validStudioId).currency("USD").build();
        settingsRepository.save(settings);

        validStudioOwnerToken = "Bearer " + jwtTokenProvider.generateAccessToken("owner@valid.com", UserRole.STUDIO_OWNER, validStudioId);
        otherStudioOwnerToken = "Bearer " + jwtTokenProvider.generateAccessToken("owner@other.com", UserRole.STUDIO_OWNER, otherStudioId);
        validStudioPhotographerToken = "Bearer " + jwtTokenProvider.generateAccessToken("photo@valid.com", UserRole.PHOTOGRAPHER, validStudioId);
    }

    @Test
    void getSettings_ShouldSucceed_WhenTenantMatches() throws Exception {
        mockMvc.perform(get("/api/v1/studios/" + validStudioId + "/settings")
                        .header("Authorization", validStudioOwnerToken))
                .andExpect(status().isOk());
    }

    @Test
    void getSettings_ShouldFail_WhenTenantDoesNotMatch() throws Exception {
        mockMvc.perform(get("/api/v1/studios/" + validStudioId + "/settings")
                        .header("Authorization", otherStudioOwnerToken))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Test
    void updateSettings_ShouldSucceed_WhenUserIsStudioOwnerOfTenant() throws Exception {
        StudioSettings updateRequest = StudioSettings.builder().currency("EUR").build();

        mockMvc.perform(put("/api/v1/studios/" + validStudioId + "/settings")
                        .header("Authorization", validStudioOwnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void updateSettings_ShouldFail_WhenUserIsPhotographerEvenIfTenantMatches() throws Exception {
        StudioSettings updateRequest = StudioSettings.builder().studioId(validStudioId).currency("EUR").build();

        mockMvc.perform(put("/api/v1/studios/" + validStudioId + "/settings")
                        .header("Authorization", validStudioPhotographerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateRequest)))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isForbidden()); // Blocked by hasRole('STUDIO_OWNER')
    }
}
