package com.shutterflow.core.studio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudioRepository extends JpaRepository<Studio, String> {
    Optional<Studio> findBySubdomain(String subdomain);
    boolean existsBySubdomain(String subdomain);
}
