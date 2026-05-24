package com.shutterflow.core.studio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudioInvitationRepository extends JpaRepository<StudioInvitation, String> {
    Optional<StudioInvitation> findByToken(String token);
}
