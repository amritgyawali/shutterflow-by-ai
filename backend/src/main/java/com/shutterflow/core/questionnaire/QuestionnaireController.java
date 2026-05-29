package com.shutterflow.core.questionnaire;

import com.shutterflow.core.common.ApiResponse;
import com.shutterflow.core.common.AppException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/studios/{studioId}/questionnaires")
@RequiredArgsConstructor
public class QuestionnaireController {

    private final QuestionnaireTemplateRepository templateRepository;
    private final QuestionnaireRepository questionnaireRepository;

    @Data
    public static class CreateTemplateRequest {
        @NotBlank
        private String name;
        private String description;
        private String eventType;
        @NotBlank
        private String schemaJson;
    }

    @Data
    public static class SendQuestionnaireRequest {
        @NotBlank
        private String templateId;
        @NotBlank
        private String clientId;
        private String bookingId;
        private LocalDate dueDate;
    }

    @Data
    public static class SubmitResponseRequest {
        @NotBlank
        private String responsesJson;
    }

    @PostMapping("/templates")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<QuestionnaireTemplate>> createTemplate(
            @PathVariable String studioId,
            @Valid @RequestBody CreateTemplateRequest request) {

        QuestionnaireTemplate template = QuestionnaireTemplate.builder()
                .id(UUID.randomUUID().toString())
                .studioId(studioId)
                .name(request.getName())
                .description(request.getDescription())
                .eventType(request.getEventType())
                .schemaJson(request.getSchemaJson())
                .build();

        return ResponseEntity.ok(ApiResponse.success(templateRepository.save(template), "Template created"));
    }

    @GetMapping("/templates")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<QuestionnaireTemplate>>> getTemplates(@PathVariable String studioId) {
        return ResponseEntity.ok(ApiResponse.success(templateRepository.findByStudioId(studioId), "Fetched templates"));
    }

    @PostMapping("/send")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Questionnaire>> sendQuestionnaire(
            @PathVariable String studioId,
            @Valid @RequestBody SendQuestionnaireRequest request) {

        Questionnaire questionnaire = Questionnaire.builder()
                .id(UUID.randomUUID().toString())
                .studioId(studioId)
                .templateId(request.getTemplateId())
                .clientId(request.getClientId())
                .bookingId(request.getBookingId())
                .dueDate(request.getDueDate())
                .status("SENT")
                .build();

        return ResponseEntity.ok(ApiResponse.success(questionnaireRepository.save(questionnaire), "Questionnaire sent"));
    }

    @GetMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<Questionnaire>>> getQuestionnaires(@PathVariable String studioId) {
        return ResponseEntity.ok(ApiResponse.success(questionnaireRepository.findByStudioId(studioId), "Fetched questionnaires"));
    }

    @PostMapping("/{questionnaireId}/submit")
    public ResponseEntity<ApiResponse<Questionnaire>> submitResponse(
            @PathVariable String studioId,
            @PathVariable String questionnaireId,
            @Valid @RequestBody SubmitResponseRequest request) {

        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new AppException("Questionnaire not found", HttpStatus.NOT_FOUND));

        questionnaire.setResponsesJson(request.getResponsesJson());
        questionnaire.setStatus("COMPLETED");
        questionnaire.setSubmittedAt(LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success(questionnaireRepository.save(questionnaire), "Response submitted"));
    }
}
