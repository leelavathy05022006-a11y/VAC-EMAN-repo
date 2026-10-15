package com.emergencymed.chatbot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_sessions")
public class ChatSession {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_active_at", nullable = false)
    private LocalDateTime lastActiveAt;

    public ChatSession() {
    }

    public ChatSession(String id, Long userId, LocalDateTime createdAt, LocalDateTime lastActiveAt) {
        this.id = id;
        this.userId = userId;
        this.createdAt = createdAt;
        this.lastActiveAt = lastActiveAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }
}
