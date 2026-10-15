package com.emergencymed.chatbot.dto;

public class ChatHistoryItemDto {
    private Long id;
    private String sender;
    private String intent;
    private String content;
    private String createdAt;

    public ChatHistoryItemDto() {
    }

    public ChatHistoryItemDto(Long id, String sender, String intent, String content, String createdAt) {
        this.id = id;
        this.sender = sender;
        this.intent = intent;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
