package com.shutterflow.core.studio;

import com.shutterflow.core.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/studios/{studioId}/settings")
@RequiredArgsConstructor
public class StudioSettingsController {

    private final StudioSettingsRepository settingsRepository;

    /**
     * Get settings for a studio.
     * Only users belonging to this studio OR an ADMIN can access it.
     */
    @GetMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<StudioSettings>> getSettings(@PathVariable String studioId) {
        StudioSettings settings = settingsRepository.findByStudioId(studioId)
                .orElseThrow(() -> new RuntimeException("Settings not found"));
        return ResponseEntity.ok(ApiResponse.success(settings, "Fetched settings successfully"));
    }

    /**
     * Update settings. Only STUDIO_OWNER of this specific studio can perform this.
     */
    @PutMapping
    @PreAuthorize("hasRole('STUDIO_OWNER') and @tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<StudioSettings>> updateSettings(
            @PathVariable String studioId,
            @RequestBody StudioSettings updateRequest) {
        // Implementation omitted for brevity
        return ResponseEntity.ok(ApiResponse.success(updateRequest, "Settings updated"));
    }
}
