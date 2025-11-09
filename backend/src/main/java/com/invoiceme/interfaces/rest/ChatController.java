package com.invoiceme.interfaces.rest;

import com.invoiceme.application.chat.ChatMessageRequest;
import com.invoiceme.application.chat.ChatMessageResponse;
import com.invoiceme.application.chat.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for AI Chat Assistant.
 * Provides conversational interface for querying invoice and customer data.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Process a chat message and return an AI-generated response.
     *
     * @param request the chat message request containing the user's message
     * @return the chat response with answer, suggestions, and conversation ID
     */
    @PostMapping("/message")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody ChatMessageRequest request) {
        ChatMessageResponse response = chatService.processMessage(request);
        return ResponseEntity.ok(response);
    }
}
