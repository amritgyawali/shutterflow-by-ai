package com.shutterflow.core.questionnaire;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionnaireRepository extends JpaRepository<Questionnaire, String> {
    List<Questionnaire> findByStudioId(String studioId);
    List<Questionnaire> findByClientId(String clientId);
    List<Questionnaire> findByBookingId(String bookingId);
    List<Questionnaire> findByStatus(String status);
}
