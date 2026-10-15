package com.emergencymed.chatbot.repository;

import com.emergencymed.chatbot.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    List<ChatMessage> findTop50BySessionIdOrderByCreatedAtAsc(String sessionId);
    long deleteByCreatedAtBefore(LocalDateTime threshold);
    long deleteBySessionIdIn(List<String> sessionIds);
}
