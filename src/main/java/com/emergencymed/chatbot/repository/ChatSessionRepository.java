package com.emergencymed.chatbot.repository;

import com.emergencymed.chatbot.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {
    List<ChatSession> findByUserIdOrderByLastActiveAtDesc(Long userId);
    Optional<ChatSession> findTopByUserIdOrderByLastActiveAtDesc(Long userId);
    long deleteByLastActiveAtBefore(LocalDateTime threshold);
    long deleteByUserId(Long userId);
}
