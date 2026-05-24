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

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final StudioService studioService;
    private final UserService userService;

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
}

