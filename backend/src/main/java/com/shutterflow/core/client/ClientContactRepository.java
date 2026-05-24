package com.shutterflow.core.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientContactRepository extends JpaRepository<ClientContact, String> {
    List<ClientContact> findByClientId(String clientId);
}
