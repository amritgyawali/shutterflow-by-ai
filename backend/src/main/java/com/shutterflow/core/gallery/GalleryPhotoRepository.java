package com.shutterflow.core.gallery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GalleryPhotoRepository extends JpaRepository<GalleryPhoto, String> {

    List<GalleryPhoto> findByGalleryIdOrderBySortOrder(String galleryId);

    List<GalleryPhoto> findByGalleryIdAndIsFavoriteTrue(String galleryId);

    long countByGalleryId(String galleryId);
}
