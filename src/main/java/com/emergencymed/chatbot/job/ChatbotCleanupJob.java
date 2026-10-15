package com.emergencymed.chatbot.job;

import com.emergencymed.chatbot.repository.ChatMessageRepository;
import com.emergencymed.chatbot.repository.ChatSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class ChatbotCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(ChatbotCleanupJob.class);

    private final ChatMessageRepository messageRepository;
    private final ChatSessionRepository sessionRepository;

    public ChatbotCleanupJob(ChatMessageRepository messageRepository, ChatSessionRepository sessionRepository) {
        this.messageRepository = messageRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Nightly scheduled cleanup job running at 03:00 AM.
     * Deletes chat messages and inactive sessions older than 30 days.
     */
    @Scheduled(cron = "${chatbot.cleanup.cron:0 0 3 * * ?}")
    @Transactional
    public void cleanOldChatHistory() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        long deletedMessages = messageRepository.deleteByCreatedAtBefore(threshold);
        long deletedSessions = sessionRepository.deleteByLastActiveAtBefore(threshold);
        log.info("[CHATBOT CLEANUP] Deleted {} messages and {} sessions older than 30 days (before {})",
                deletedMessages, deletedSessions, threshold);
    }
}
