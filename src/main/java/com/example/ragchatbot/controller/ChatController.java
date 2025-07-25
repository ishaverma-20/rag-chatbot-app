package com.example.ragchatbot.controller;

import com.example.ragchatbot.model.ChatRequest;
import com.example.ragchatbot.model.ChatResponse;
import com.example.ragchatbot.service.RagService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ChatController {

    private final RagService ragService;
    private final ChatClient chatClient;

    public ChatController(RagService ragService, ChatClient.Builder chatClientBuilder) {
        this.ragService = ragService;
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/api/auth/status")
    public Map<String, Boolean> getAuthStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
        return Map.of("isAuthenticated", isLoggedIn);
    }

    @PostMapping("/api/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String llmResponse = chatClient.prompt()
                .user(request.message())
                .call()
                .content();
        return new ChatResponse(llmResponse);
    }

    @PostMapping("/api/rag-chat")
    public ChatResponse ragChat(@RequestBody ChatRequest request) throws Exception {
        String ragResponse = ragService.chatWithRag(request.message());
        return new ChatResponse(ragResponse);
    }
}