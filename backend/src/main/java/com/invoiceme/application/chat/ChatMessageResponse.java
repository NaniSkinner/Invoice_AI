package com.invoiceme.application.chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for chat messages.
 * Contains the AI assistant's response, suggested follow-up actions, and conversation ID.
 */
public class ChatMessageResponse {

    private String response;
    private List<String> suggestions;
    private String conversationId;

    public ChatMessageResponse() {
        this.suggestions = new ArrayList<>();
    }

    public ChatMessageResponse(String response, List<String> suggestions, String conversationId) {
        this.response = response;
        this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
        this.conversationId = conversationId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
