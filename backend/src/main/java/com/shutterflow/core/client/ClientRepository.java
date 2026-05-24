package com.shutterflow.core.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, String>, JpaSpecificationExecutor<Client> {
    List<Client> findByStudioId(String studioId);
    Optional<Client> findByEmailAndStudioId(String email, String studioId);
    boolean existsByEmailAndStudioId(String email, String studioId);
}
