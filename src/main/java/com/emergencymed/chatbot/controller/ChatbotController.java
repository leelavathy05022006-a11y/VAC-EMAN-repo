package com.emergencymed.chatbot.controller;

import com.emergencymed.chatbot.dto.ChatHistoryItemDto;
import com.emergencymed.chatbot.dto.ChatbotMessageRequest;
import com.emergencymed.chatbot.dto.ChatbotMessageResponse;
import com.emergencymed.chatbot.service.ChatbotService;
import com.emergencymed.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    /**
     * POST /api/v1/chatbot/message
     * Processes a user chat message through validation, intent detection, domain query, and verified response.
     */
    @PostMapping("/message")
    public ApiResponse<ChatbotMessageResponse> sendMessage(@Valid @RequestBody ChatbotMessageRequest request,
                                                           HttpServletRequest httpRequest) {
        String clientIp = extractClientIp(httpRequest);
        ChatbotMessageResponse response = chatbotService.processMessage(request, clientIp);
        return ApiResponse.ok("Response generated successfully", response);
    }

    /**
     * GET /api/v1/chatbot/history
     * Retrieves chat message history for the authenticated user's current session.
     */
    @GetMapping("/history")
    public ApiResponse<List<ChatHistoryItemDto>> getHistory() {
        List<ChatHistoryItemDto> history = chatbotService.getUserHistory();
        return ApiResponse.ok("Chat history retrieved successfully", history);
    }

    /**
     * DELETE /api/v1/chatbot/history
     * Clears chat history for the authenticated user.
     */
    @DeleteMapping("/history")
    public ApiResponse<Map<String, Object>> clearHistory() {
        long deletedCount = chatbotService.clearUserHistory();
        return ApiResponse.ok("Chat history cleared successfully", Map.of("deletedSessions", deletedCount));
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";
        String xForwarded = request.getHeader("X-Forwarded-For");
        if (xForwarded != null && !xForwarded.isBlank()) {
            return xForwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}
