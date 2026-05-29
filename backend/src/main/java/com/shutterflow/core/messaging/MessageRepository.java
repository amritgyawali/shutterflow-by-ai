package com.shutterflow.core.messaging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByBookingIdOrderByCreatedAtAsc(String bookingId);
    List<Message> findByRecipientIdAndIsReadFalse(String recipientId);
    List<Message> findBySenderIdOrRecipientIdOrderByCreatedAtDesc(String senderId, String recipientId);
}
