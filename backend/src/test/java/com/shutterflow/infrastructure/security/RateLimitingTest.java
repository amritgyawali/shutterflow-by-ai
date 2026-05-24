package com.shutterflow.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.test.context.TestPropertySource(properties = {"bucket4j.enabled=true"})
class RateLimitingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Clear caches before test
        cacheManager.getCache("rate-limit-cache").clear();
    }

    @Test
    void authEndpoints_ShouldBeRateLimitedAndBlockedAfter5Attempts() throws Exception {
        String mockLoginJson = "{\"email\":\"test@shutterflow.com\",\"password\":\"wrongpassword\"}";

        // Perform 5 successful checkouts/requests (should return 401 or whatever the auth returns, but NOT 429)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mockLoginJson)
                            .with(request -> {
                                request.setRemoteAddr("192.168.1.100");
                                return request;
                            }))
                    .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                    .andExpect(status().is(org.hamcrest.Matchers.oneOf(401, 200, 404))); // Auth might fail or pass, but not 429
        }

        // The 6th attempt from the same IP must be rate limited with HTTP 429
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mockLoginJson)
                        .with(request -> {
                            request.setRemoteAddr("192.168.1.100");
                            return request;
                        }))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Too many requests. You have been blocked for 30 minutes."));

        // A request from a DIFFERENT IP should still succeed
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mockLoginJson)
                        .with(request -> {
                            request.setRemoteAddr("192.168.1.200");
                            return request;
                        }))
                .andExpect(status().is(org.hamcrest.Matchers.oneOf(401, 200, 404)));
    }
}
