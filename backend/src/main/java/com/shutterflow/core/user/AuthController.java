package com.shutterflow.core.user;

import com.shutterflow.core.common.ApiResponse;
import com.shutterflow.core.studio.StudioService;
import com.shutterflow.core.user.dto.AuthResponse;
import com.shutterflow.core.user.dto.RegisterStudioRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shutterflow.core.user.dto.RegisterPhotographerRequest;
import com.shutterflow.core.user.dto.MagicLinkRequest;
import com.shutterflow.core.user.dto.MagicLoginRequest;
import com.shutterflow.core.user.dto.ForgotPasswordRequest;
import com.shutterflow.core.user.dto.ResetPasswordRequest;
import com.shutterflow.infrastructure.mail.EmailService;
import com.shutterflow.infrastructure.security.JwtTokenProvider;
import com.shutterflow.infrastructure.security.MagicTokenService;
import com.shutterflow.infrastructure.security.PasswordResetTokenService;
import com.shutterflow.core.common.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final StudioService studioService;
    private final UserService userService;
    private final MagicTokenService magicTokenService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final com.shutterflow.core.studio.StudioInvitationRepository studioInvitationRepository;

    @PostMapping("/register-studio")
    public ResponseEntity<ApiResponse<AuthResponse>> registerStudio(
            @Valid @RequestBody RegisterStudioRequest request) {
        AuthResponse response = studioService.registerStudio(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Studio registered successfully"));
    }

    @PostMapping("/register-photographer")
    public ResponseEntity<ApiResponse<AuthResponse>> registerPhotographer(
            @Valid @RequestBody RegisterPhotographerRequest request) {
        AuthResponse response = userService.registerPhotographer(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Photographer registered successfully"));
    }

    @PostMapping("/magic-request")
    public ResponseEntity<ApiResponse<Void>> requestMagicLink(@Valid @RequestBody MagicLinkRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        String token = magicTokenService.generateAndStoreMagicToken(user.getEmail());
        String magicLink = "http://localhost:4200/auth/magic-login?token=" + token; // Default local URL
        emailService.sendMagicLinkEmail(user.getEmail(), magicLink);

        return ResponseEntity.ok(ApiResponse.success(null, "Magic link sent successfully"));
    }

    @PostMapping("/magic-login")
    public ResponseEntity<ApiResponse<AuthResponse>> magicLogin(@Valid @RequestBody MagicLoginRequest request) {
        String email = magicTokenService.validateAndConsumeToken(request.getToken());
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getEmail(), user.getRole(), user.getStudioId()
        );
        String refreshToken = UUID.randomUUID().toString();

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .role(user.getRole())
                .studioId(user.getStudioId())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Logged in successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        String token = passwordResetTokenService.generateAndStoreResetToken(user.getEmail());
        String resetLink = "http://localhost:4200/auth/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

        return ResponseEntity.ok(ApiResponse.success(null, "Password reset link sent successfully"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String email = passwordResetTokenService.validateAndConsumeToken(request.getToken());
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"));
    }

    @lombok.Data
    public static class AcceptInviteRequest {
        @jakarta.validation.constraints.NotBlank
        private String token;
        @jakarta.validation.constraints.NotBlank
        private String password;
    }

    @PostMapping("/accept-invite")
    public ResponseEntity<ApiResponse<AuthResponse>> acceptInvite(@Valid @RequestBody AcceptInviteRequest request) {
        com.shutterflow.core.studio.StudioInvitation invitation = studioInvitationRepository.findByToken(request.getToken())
                .orElseThrow(() -> new AppException("Invalid or expired invitation token", HttpStatus.BAD_REQUEST));

        if (invitation.isRedeemed()) {
            throw new AppException("Invitation token has already been redeemed", HttpStatus.BAD_REQUEST);
        }

        if (java.time.LocalDateTime.now().isAfter(invitation.getExpiresAt())) {
            throw new AppException("Invitation token has expired", HttpStatus.BAD_REQUEST);
        }

        // Check if user already exists
        User user = userRepository.findByEmail(invitation.getEmail().toLowerCase()).orElse(null);
        if (user != null) {
            if (user.getStudioId() != null) {
                throw new AppException("User is already linked to a studio space", HttpStatus.BAD_REQUEST);
            }
            user.setStudioId(invitation.getStudioId());
            user.setRole(UserRole.valueOf(invitation.getRole()));
            userRepository.save(user);
        } else {
            user = User.builder()
                    .id(UUID.randomUUID().toString())
                    .email(invitation.getEmail().toLowerCase())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .role(UserRole.valueOf(invitation.getRole()))
                    .studioId(invitation.getStudioId())
                    .build();
            userRepository.save(user);
        }

        invitation.setRedeemed(true);
        studioInvitationRepository.save(invitation);

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getEmail(), user.getRole(), user.getStudioId()
        );
        String refreshToken = UUID.randomUUID().toString();

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .role(user.getRole())
                .studioId(user.getStudioId())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Invitation accepted and joined studio team successfully"));
    }
}

