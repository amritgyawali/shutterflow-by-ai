package com.shutterflow.core.messaging;

import com.shutterflow.core.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/studios/{studioId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;

    @Data
    public static class SendMessageRequest {
        private String bookingId;
        @NotBlank
        private String senderType;
        @NotBlank
        private String senderId;
        @NotBlank
        private String recipientId;
        @NotBlank
        private String content;
    }

    @PostMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Message>> sendMessage(
            @PathVariable String studioId,
            @Valid @RequestBody SendMessageRequest request) {

        Message message = Message.builder()
                .id(UUID.randomUUID().toString())
                .studioId(studioId)
                .bookingId(request.getBookingId())
                .senderType(request.getSenderType())
                .senderId(request.getSenderId())
                .recipientId(request.getRecipientId())
                .content(request.getContent())
                .build();

        Message saved = messageRepository.save(message);

        // Create notification for recipient
        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .studioId(studioId)
                .userId(request.getRecipientId())
                .type("NEW_MESSAGE")
                .title("New message")
                .body(request.getContent().length() > 100 ? request.getContent().substring(0, 100) + "..." : request.getContent())
                .channel("IN_APP")
                .build();
        notificationRepository.save(notification);

        return ResponseEntity.ok(ApiResponse.success(saved, "Message sent"));
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<Message>>> getBookingMessages(
            @PathVariable String studioId,
            @PathVariable String bookingId) {
        return ResponseEntity.ok(ApiResponse.success(
                messageRepository.findByBookingIdOrderByCreatedAtAsc(bookingId), "Fetched messages"));
    }

    @PatchMapping("/{messageId}/read")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Message>> markAsRead(
            @PathVariable String studioId,
            @PathVariable String messageId) {
        Message message = messageRepository.findById(messageId).orElse(null);
        if (message != null) {
            message.setRead(true);
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);
        }
        return ResponseEntity.ok(ApiResponse.success(message, "Marked as read"));
    }

    @GetMapping("/notifications/{userId}")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(
            @PathVariable String studioId,
            @PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId), "Fetched notifications"));
    }
}
