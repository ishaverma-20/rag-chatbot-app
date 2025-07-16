package com.example.ragchatbot.controller;

import com.example.ragchatbot.model.ChatRequest;
import com.example.ragchatbot.model.ChatResponse;
import com.example.ragchatbot.service.RagService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final RagService ragService;
    private final ChatClient chatClient; // Injected for direct LLM interaction

    public ChatController(RagService ragService, ChatClient.Builder chatClientBuilder) {
        this.ragService = ragService;
        this.chatClient = chatClientBuilder.build(); // Build the ChatClient from the builder
    }

    @PostMapping("/api/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        // For initial basic inference, we can directly call the chatClient.
        String llmResponse = chatClient.prompt()
                .user(request.message())
                .call()
                .content();
        return new ChatResponse(llmResponse);
    }

    // This method will be used for RAG once implemented in RagService
    @PostMapping("/api/rag-chat")
    public ChatResponse ragChat(@RequestBody ChatRequest request) {
        String ragResponse = ragService.chatWithRag(request.message());
        return new ChatResponse(ragResponse);
    }
}