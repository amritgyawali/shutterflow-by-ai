package com.shutterflow.core.questionnaire;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionnaireTemplateRepository extends JpaRepository<QuestionnaireTemplate, String> {
    List<QuestionnaireTemplate> findByStudioId(String studioId);
}
