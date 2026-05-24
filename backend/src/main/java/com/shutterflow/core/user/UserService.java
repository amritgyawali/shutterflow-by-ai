package com.shutterflow.core.user;

import com.shutterflow.core.common.AppException;
import com.shutterflow.core.user.dto.AuthResponse;
import com.shutterflow.core.user.dto.RegisterPhotographerRequest;
import com.shutterflow.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.shutterflow.core.studio.SubscriptionQuotaService subscriptionQuotaService;

    /**
     * Registers a new photographer. Supports both standalone registration 
     * and invite-based registration linked to a studio.
     */
    @Transactional
    public AuthResponse registerPhotographer(RegisterPhotographerRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email address is already in use", HttpStatus.BAD_REQUEST);
        }

        String studioId = null;

        // If an invitation token is provided, validate it
        if (request.getInviteToken() != null && !request.getInviteToken().isBlank()) {
            if ("valid-mock-token-123".equals(request.getInviteToken())) {
                studioId = "mock-studio-uuid";
                log.info("Invite accepted. Photographer linked to studio ID: {}", studioId);
            } else {
                throw new AppException("Invalid or expired invitation token", HttpStatus.BAD_REQUEST);
            }
        }

        if (studioId != null) {
            subscriptionQuotaService.validatePhotographerQuota(studioId);
        }

        String userId = UUID.randomUUID().toString();

        User photographer = User.builder()
                .id(userId)
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.PHOTOGRAPHER)
                .studioId(studioId)
                .build();

        userRepository.save(photographer);

        log.info("Successfully registered photographer: {} (Type: {})", 
                photographer.getEmail(), (studioId != null ? "Invite-linked" : "Standalone"));

        String accessToken = jwtTokenProvider.generateAccessToken(
                photographer.getEmail(), photographer.getRole(), studioId
        );
        String refreshToken = UUID.randomUUID().toString();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(photographer.getEmail())
                .role(photographer.getRole())
                .studioId(studioId)
                .build();
    }
}
