package com.emergencymed.chatbot.llm;

import com.emergencymed.chatbot.dto.ExtractedQueryEntities;

public interface ChatbotLlmClient {
    boolean isAvailable();
    ExtractedQueryEntities classifyAndExtract(String userText);
    String rephraseReply(String rawReply);
}
