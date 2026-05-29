package com.shutterflow.core.gallery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GalleryRepository extends JpaRepository<Gallery, String> {

    List<Gallery> findByStudioId(String studioId);

    List<Gallery> findByClientId(String clientId);

    Optional<Gallery> findByShareToken(String shareToken);

    List<Gallery> findByBookingId(String bookingId);
}
