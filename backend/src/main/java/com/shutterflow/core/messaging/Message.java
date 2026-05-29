package com.shutterflow.core.messaging;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "studio_id", nullable = false, length = 36)
    private String studioId;

    @Column(name = "booking_id", length = 36)
    private String bookingId;

    @Column(name = "sender_type", nullable = false, length = 20)
    private String senderType; // USER or CLIENT

    @Column(name = "sender_id", nullable = false, length = 36)
    private String senderId;

    @Column(name = "recipient_id", nullable = false, length = 36)
    private String recipientId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "attachment_s3_key", length = 500)
    private String attachmentS3Key;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
