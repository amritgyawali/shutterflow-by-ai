package com.shutterflow.core.studio;

import com.shutterflow.core.common.ApiResponse;
import com.shutterflow.core.common.AppException;
import com.shutterflow.infrastructure.aws.S3Service;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/studios/{studioId}")
@RequiredArgsConstructor
@Slf4j
public class StudioController {

    private final StudioRepository studioRepository;
    private final S3Service s3Service;
    private final StudioInvitationRepository studioInvitationRepository;
    private final SubscriptionQuotaService subscriptionQuotaService;
    private final com.shutterflow.infrastructure.mail.EmailService emailService;
    private final com.shutterflow.core.user.UserRepository userRepository;
    private final com.shutterflow.core.user.PhotographerProfileRepository photographerProfileRepository;

    @Data
    public static class InviteRequest {
        private String email;
        private String role; // Optional, default PHOTOGRAPHER
    }

    /**
     * Invite a photographer to join the studio team.
     * Enforces plan limits and checks subscription photographer quotas first!
     */
    @PostMapping("/invite")
    @PreAuthorize("hasRole('STUDIO_OWNER') and @tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Void>> invitePhotographer(
            @PathVariable String studioId,
            @RequestBody InviteRequest request) {

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new AppException("Email is required for invitation", HttpStatus.BAD_REQUEST);
        }

        // 1. Enforce subscription tier photographer limit quotas first!
        subscriptionQuotaService.validatePhotographerQuota(studioId);

        String role = (request.getRole() != null && !request.getRole().isBlank()) 
                ? request.getRole().toUpperCase() 
                : "PHOTOGRAPHER";

        // 2. Generate secure invitation token
        String token = UUIDString() + UUIDString();
        java.time.LocalDateTime expiresAt = java.time.LocalDateTime.now().plusDays(2); // 48 hours validity

        StudioInvitation invitation = StudioInvitation.builder()
                .token(token)
                .studioId(studioId)
                .email(request.getEmail().toLowerCase())
                .role(role)
                .expiresAt(expiresAt)
                .redeemed(false)
                .build();

        studioInvitationRepository.save(invitation);

        // 3. Dispatch invitation email via SendGrid
        String inviteLink = "http://localhost:4200/studio/join?token=" + token;
        emailService.sendInvitationEmail(request.getEmail(), inviteLink);

        log.info("Dispatched invitation to: {} for studio: {} with role: {}", request.getEmail(), studioId, role);

        return ResponseEntity.ok(ApiResponse.success(null, "Invitation sent successfully"));
    }

    @Data
    @Builder
    public static class StudioBrandingResponse {
        private String id;
        private String name;
        private String subdomain;
        private String logoUrl;
        private String primaryColor;
        private String secondaryColor;
        private String customFont;
    }

    /**
     * Get studio branding details.
     * Accessible by anyone since branding is a public resource for clients.
     */
    @GetMapping("/branding")
    public ResponseEntity<ApiResponse<StudioBrandingResponse>> getBranding(@PathVariable String studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new AppException("Studio not found", HttpStatus.NOT_FOUND));

        String logoUrl = null;
        if (studio.getLogoS3Key() != null && !studio.getLogoS3Key().isEmpty()) {
            logoUrl = s3Service.generatePreSignedUrl(studio.getLogoS3Key(), Duration.ofHours(1));
        }

        StudioBrandingResponse response = StudioBrandingResponse.builder()
                .id(studio.getId())
                .name(studio.getName())
                .subdomain(studio.getSubdomain())
                .logoUrl(logoUrl)
                .primaryColor(studio.getPrimaryColor())
                .secondaryColor(studio.getSecondaryColor())
                .customFont(studio.getCustomFont())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Fetched studio branding details"));
    }

    /**
     * Update branding customization.
     * Restricted to STUDIO_OWNER of this specific studio.
     */
    @PatchMapping("/branding")
    @PreAuthorize("hasRole('STUDIO_OWNER') and @tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<StudioBrandingResponse>> updateBranding(
            @PathVariable String studioId,
            @RequestParam(required = false) String primaryColor,
            @RequestParam(required = false) String secondaryColor,
            @RequestParam(required = false) String customFont,
            @RequestParam(required = false) MultipartFile logoFile) {

        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new AppException("Studio not found", HttpStatus.NOT_FOUND));

        if (primaryColor != null) {
            studio.setPrimaryColor(primaryColor);
        }
        if (secondaryColor != null) {
            studio.setSecondaryColor(secondaryColor);
        }
        if (customFont != null) {
            studio.setCustomFont(customFont);
        }

        if (logoFile != null && !logoFile.isEmpty()) {
            String key = "studios/" + studioId + "/logo/" + UUIDString() + "_" + logoFile.getOriginalFilename();
            try {
                s3Service.uploadFile(key, logoFile.getBytes(), logoFile.getContentType());
                
                // Clean up old logo if it exists
                if (studio.getLogoS3Key() != null && !studio.getLogoS3Key().isEmpty()) {
                    try {
                        s3Service.deleteFile(studio.getLogoS3Key());
                    } catch (Exception e) {
                        log.warn("Failed to delete old S3 logo: {}", studio.getLogoS3Key());
                    }
                }
                
                studio.setLogoS3Key(key);
            } catch (IOException e) {
                log.error("Failed to read logo upload bytes", e);
                throw new AppException("Failed to process logo file upload", HttpStatus.BAD_REQUEST);
            }
        }

        studioRepository.save(studio);

        String logoUrl = null;
        if (studio.getLogoS3Key() != null && !studio.getLogoS3Key().isEmpty()) {
            logoUrl = s3Service.generatePreSignedUrl(studio.getLogoS3Key(), Duration.ofHours(1));
        }

        StudioBrandingResponse response = StudioBrandingResponse.builder()
                .id(studio.getId())
                .name(studio.getName())
                .subdomain(studio.getSubdomain())
                .logoUrl(logoUrl)
                .primaryColor(studio.getPrimaryColor())
                .secondaryColor(studio.getSecondaryColor())
                .customFont(studio.getCustomFont())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Studio branding updated successfully"));
    }

    @Data
    @Builder
    public static class TeamMemberResponse {
        private String userId;
        private String email;
        private String role;
        private String bio;
        private String specializations;
        private String availabilityHours;
        private String status;
        private java.util.List<String> portfolioUrls;
    }

    /**
     * Get all team photographers of this studio.
     */
    @GetMapping("/photographers")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<java.util.List<TeamMemberResponse>>> getTeamPhotographers(
            @PathVariable String studioId) {

        java.util.List<com.shutterflow.core.user.User> users = userRepository.findByStudioId(studioId);
        java.util.List<TeamMemberResponse> responseList = new java.util.ArrayList<>();

        for (com.shutterflow.core.user.User u : users) {
            if (u.getRole() == com.shutterflow.core.user.UserRole.STUDIO_OWNER || 
                u.getRole() == com.shutterflow.core.user.UserRole.PHOTOGRAPHER ||
                u.getRole() == com.shutterflow.core.user.UserRole.SECOND_SHOOTER) {

                com.shutterflow.core.user.PhotographerProfile profile = photographerProfileRepository.findById(u.getId())
                        .orElseGet(() -> {
                            com.shutterflow.core.user.PhotographerProfile p = new com.shutterflow.core.user.PhotographerProfile();
                            p.setUserId(u.getId());
                            p.setStatus("ACTIVE");
                            return p;
                        });

                java.util.List<String> urls = new java.util.ArrayList<>();
                if (profile.getPortfolioS3Keys() != null && !profile.getPortfolioS3Keys().isBlank()) {
                    String[] keys = profile.getPortfolioS3Keys().split(",");
                    for (String k : keys) {
                        urls.add(s3Service.generatePreSignedUrl(k.trim(), Duration.ofHours(1)));
                    }
                }

                responseList.add(TeamMemberResponse.builder()
                        .userId(u.getId())
                        .email(u.getEmail())
                        .role(u.getRole().name())
                        .bio(profile.getBio())
                        .specializations(profile.getSpecializations())
                        .availabilityHours(profile.getAvailabilityHours())
                        .status(profile.getStatus())
                        .portfolioUrls(urls)
                        .build());
            }
        }

        return ResponseEntity.ok(ApiResponse.success(responseList, "Fetched team photographers successfully"));
    }

    /**
     * Update the active status of a photographer. Only STUDIO_OWNER can perform this.
     */
    @PutMapping("/photographers/{userId}/status")
    @PreAuthorize("hasRole('STUDIO_OWNER') and @tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<TeamMemberResponse>> updatePhotographerStatus(
            @PathVariable String studioId,
            @PathVariable String userId,
            @RequestParam String status) {

        com.shutterflow.core.user.User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        if (!studioId.equals(user.getStudioId())) {
            throw new AppException("User does not belong to this studio space", HttpStatus.BAD_REQUEST);
        }

        com.shutterflow.core.user.PhotographerProfile profile = photographerProfileRepository.findById(userId)
                .orElseGet(() -> {
                    com.shutterflow.core.user.PhotographerProfile p = new com.shutterflow.core.user.PhotographerProfile();
                    p.setUserId(userId);
                    return p;
                });

        profile.setStatus(status.toUpperCase());
        photographerProfileRepository.save(profile);

        TeamMemberResponse response = TeamMemberResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(profile.getStatus())
                .bio(profile.getBio())
                .specializations(profile.getSpecializations())
                .availabilityHours(profile.getAvailabilityHours())
                .portfolioUrls(new java.util.ArrayList<>())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Photographer status updated successfully"));
    }

    /**
     * Update photographer portfolio bio, availability settings, and upload multiple portfolio files.
     */
    @PatchMapping("/photographers/{userId}/profile")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<TeamMemberResponse>> updatePhotographerProfile(
            @PathVariable String studioId,
            @PathVariable String userId,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String specializations,
            @RequestParam(required = false) String availabilityHours,
            @RequestParam(required = false) MultipartFile[] portfolioFiles) {

        com.shutterflow.core.user.User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        if (!studioId.equals(user.getStudioId())) {
            throw new AppException("User does not belong to this studio space", HttpStatus.BAD_REQUEST);
        }

        com.shutterflow.core.user.PhotographerProfile profile = photographerProfileRepository.findById(userId)
                .orElseGet(() -> {
                    com.shutterflow.core.user.PhotographerProfile p = new com.shutterflow.core.user.PhotographerProfile();
                    p.setUserId(userId);
                    p.setStatus("ACTIVE");
                    return p;
                });

        if (bio != null) {
            profile.setBio(bio);
        }
        if (specializations != null) {
            profile.setSpecializations(specializations);
        }
        if (availabilityHours != null) {
            profile.setAvailabilityHours(availabilityHours);
        }

        java.util.List<String> newKeysList = new java.util.ArrayList<>();
        if (profile.getPortfolioS3Keys() != null && !profile.getPortfolioS3Keys().isBlank()) {
            newKeysList.addAll(java.util.List.of(profile.getPortfolioS3Keys().split(",")));
        }

        if (portfolioFiles != null) {
            for (MultipartFile file : portfolioFiles) {
                if (!file.isEmpty()) {
                    String key = "studios/" + studioId + "/photographers/" + userId + "/" + UUIDString() + "_" + file.getOriginalFilename();
                    try {
                        s3Service.uploadFile(key, file.getBytes(), file.getContentType());
                        newKeysList.add(key);
                    } catch (IOException e) {
                        log.error("Failed to upload portfolio picture", e);
                        throw new AppException("Failed to process portfolio file upload", HttpStatus.BAD_REQUEST);
                    }
                }
            }
        }

        if (!newKeysList.isEmpty()) {
            profile.setPortfolioS3Keys(String.join(",", newKeysList));
        }

        photographerProfileRepository.save(profile);

        java.util.List<String> urls = new java.util.ArrayList<>();
        for (String k : newKeysList) {
            urls.add(s3Service.generatePreSignedUrl(k.trim(), Duration.ofHours(1)));
        }

        TeamMemberResponse response = TeamMemberResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .bio(profile.getBio())
                .specializations(profile.getSpecializations())
                .availabilityHours(profile.getAvailabilityHours())
                .status(profile.getStatus())
                .portfolioUrls(urls)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Photographer profile updated successfully"));
    }

    private String UUIDString() {
        return java.util.UUID.randomUUID().toString();
    }
}
