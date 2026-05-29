package com.shutterflow.core.gallery;

import com.shutterflow.core.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/studios/{studioId}/galleries")
@RequiredArgsConstructor
public class GalleryController {

    private final GalleryService galleryService;

    @Data
    public static class CreateGalleryRequest {
        @NotBlank
        private String clientId;
        private String bookingId;
        @NotBlank
        private String title;
        private String description;
    }

    @PostMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Gallery>> createGallery(
            @PathVariable String studioId,
            @Valid @RequestBody CreateGalleryRequest request) {

        Gallery gallery = galleryService.createGallery(
                studioId, request.getClientId(), request.getBookingId(),
                request.getTitle(), request.getDescription());

        return ResponseEntity.ok(ApiResponse.success(gallery, "Gallery created successfully"));
    }

    @GetMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<Gallery>>> getGalleries(@PathVariable String studioId) {
        List<Gallery> galleries = galleryService.getStudioGalleries(studioId);
        return ResponseEntity.ok(ApiResponse.success(galleries, "Fetched galleries"));
    }

    @GetMapping("/{galleryId}")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Gallery>> getGallery(
            @PathVariable String studioId,
            @PathVariable String galleryId) {
        Gallery gallery = galleryService.getGallery(galleryId);
        return ResponseEntity.ok(ApiResponse.success(gallery, "Fetched gallery"));
    }

    @PostMapping("/{galleryId}/photos")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<GalleryPhoto>> uploadPhoto(
            @PathVariable String studioId,
            @PathVariable String galleryId,
            @RequestParam("file") MultipartFile file) {

        GalleryPhoto photo = galleryService.uploadPhoto(galleryId, file);
        return ResponseEntity.ok(ApiResponse.success(photo, "Photo uploaded successfully"));
    }

    @GetMapping("/{galleryId}/photos")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<GalleryPhoto>>> getPhotos(
            @PathVariable String studioId,
            @PathVariable String galleryId) {
        List<GalleryPhoto> photos = galleryService.getGalleryPhotos(galleryId);
        return ResponseEntity.ok(ApiResponse.success(photos, "Fetched photos"));
    }

    @PostMapping("/{galleryId}/publish")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Gallery>> publishGallery(
            @PathVariable String studioId,
            @PathVariable String galleryId) {
        Gallery published = galleryService.publishGallery(galleryId);
        return ResponseEntity.ok(ApiResponse.success(published, "Gallery published"));
    }

    @PatchMapping("/{galleryId}/photos/{photoId}/favorite")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<GalleryPhoto>> toggleFavorite(
            @PathVariable String studioId,
            @PathVariable String galleryId,
            @PathVariable String photoId) {
        GalleryPhoto photo = galleryService.toggleFavorite(photoId);
        return ResponseEntity.ok(ApiResponse.success(photo, "Favorite toggled"));
    }
}
