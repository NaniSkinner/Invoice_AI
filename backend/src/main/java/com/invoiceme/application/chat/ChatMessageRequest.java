package com.invoiceme.application.chat;

/**
 * Request DTO for chat messages.
 * Contains the user's message and optional conversation context.
 */
public class ChatMessageRequest {

    private String message;
    private String conversationId;

    public ChatMessageRequest() {
    }

    public ChatMessageRequest(String message, String conversationId) {
        this.message = message;
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
