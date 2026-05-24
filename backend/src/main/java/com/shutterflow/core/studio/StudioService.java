package com.shutterflow.core.studio;

import com.shutterflow.core.common.AppException;
import com.shutterflow.core.user.User;
import com.shutterflow.core.user.UserRepository;
import com.shutterflow.core.user.UserRole;
import com.shutterflow.core.user.dto.AuthResponse;
import com.shutterflow.core.user.dto.RegisterStudioRequest;
import com.shutterflow.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudioService {

    private final StudioRepository studioRepository;
    private final StudioSettingsRepository studioSettingsRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Executes the transaction-safe onboarding flow for a new studio and its owner.
     * Generates a signed JWT session to log in the owner automatically.
     */
    @Transactional
    public AuthResponse registerStudio(RegisterStudioRequest request) {
        // 1. Double check constraints
        if (studioRepository.existsBySubdomain(request.getSubdomain())) {
            throw new AppException("Subdomain is already registered by another studio", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(request.getOwnerEmail())) {
            throw new AppException("Email address is already in use by another account", HttpStatus.BAD_REQUEST);
        }

        // 2. Generate transaction IDs
        String studioId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        // 3. Persist Studio record
        Studio studio = Studio.builder()
                .id(studioId)
                .name(request.getStudioName())
                .subdomain(request.getSubdomain())
                .planTier(PlanTier.STARTER)
                .build();
        studioRepository.save(studio);

        // 4. Persist default StudioSettings
        StudioSettings settings = StudioSettings.builder()
                .studioId(studioId)
                .currency("AUD")
                .taxRate(new BigDecimal("10.00"))
                .primaryColor("#1f2937")
                .secondaryColor("#10b981")
                .build();
        studioSettingsRepository.save(settings);

        // 5. Hash owner credentials and persist User record
        User owner = User.builder()
                .id(userId)
                .email(request.getOwnerEmail())
                .passwordHash(passwordEncoder.encode(request.getOwnerPassword()))
                .role(UserRole.STUDIO_OWNER)
                .studioId(studioId)
                .build();
        userRepository.save(owner);

        log.info("Successfully onboarded studio: {} (subdomain: {}) for owner: {}", 
                request.getStudioName(), request.getSubdomain(), request.getOwnerEmail());

        // 6. Generate active access session details
        String accessToken = jwtTokenProvider.generateAccessToken(owner.getEmail(), owner.getRole(), studioId);
        String refreshToken = UUID.randomUUID().toString(); // Persisted in Redis in Sprint 2 Day 4

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(owner.getEmail())
                .role(owner.getRole())
                .studioId(studioId)
                .build();
    }
}
