package com.shutterflow.core.gallery;

import com.shutterflow.core.common.AppException;
import com.shutterflow.infrastructure.aws.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GalleryService {

    private final GalleryRepository galleryRepository;
    private final GalleryPhotoRepository photoRepository;
    private final S3Service s3Service;

    @Transactional
    public Gallery createGallery(String studioId, String clientId, String bookingId, String title, String description) {
        Gallery gallery = Gallery.builder()
                .id(UUID.randomUUID().toString())
                .studioId(studioId)
                .clientId(clientId)
                .bookingId(bookingId)
                .title(title)
                .description(description)
                .shareToken(UUID.randomUUID().toString())
                .status("DRAFT")
                .build();

        return galleryRepository.save(gallery);
    }

    @Transactional
    public GalleryPhoto uploadPhoto(String galleryId, MultipartFile file) {
        Gallery gallery = getGallery(galleryId);

        String key = String.format("galleries/%s/originals/%s_%s",
                galleryId, UUID.randomUUID(), file.getOriginalFilename());

        try {
            s3Service.uploadFile(key, file.getBytes(), file.getContentType());
        } catch (IOException e) {
            throw new AppException("Failed to upload photo", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Generate thumbnail key (processing would happen async in production)
        String thumbnailKey = key.replace("/originals/", "/thumbnails/");

        long photoCount = photoRepository.countByGalleryId(galleryId);

        GalleryPhoto photo = GalleryPhoto.builder()
                .id(UUID.randomUUID().toString())
                .galleryId(galleryId)
                .originalS3Key(key)
                .thumbnailS3Key(thumbnailKey)
                .filename(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .sortOrder((int) photoCount)
                .build();

        return photoRepository.save(photo);
    }

    @Transactional
    public Gallery publishGallery(String galleryId) {
        Gallery gallery = getGallery(galleryId);
        gallery.setStatus("PUBLISHED");
        return galleryRepository.save(gallery);
    }

    public Gallery getGallery(String galleryId) {
        return galleryRepository.findById(galleryId)
                .orElseThrow(() -> new AppException("Gallery not found", HttpStatus.NOT_FOUND));
    }

    public Gallery getGalleryByShareToken(String shareToken) {
        return galleryRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new AppException("Gallery not found", HttpStatus.NOT_FOUND));
    }

    public List<Gallery> getStudioGalleries(String studioId) {
        return galleryRepository.findByStudioId(studioId);
    }

    public List<GalleryPhoto> getGalleryPhotos(String galleryId) {
        return photoRepository.findByGalleryIdOrderBySortOrder(galleryId);
    }

    public String getPhotoUrl(String s3Key) {
        return s3Service.generatePreSignedUrl(s3Key, Duration.ofHours(2));
    }

    @Transactional
    public GalleryPhoto toggleFavorite(String photoId) {
        GalleryPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new AppException("Photo not found", HttpStatus.NOT_FOUND));
        photo.setFavorite(!photo.isFavorite());
        return photoRepository.save(photo);
    }
}
