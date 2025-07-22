package com.example.ragchatbot.controller;

import com.example.ragchatbot.model.ChatRequest;
import com.example.ragchatbot.model.ChatResponse;
import com.example.ragchatbot.service.RagService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatController {

    private final RagService ragService;
    private final ChatClient chatClient;
    private final VectorStore vectorStore; // Inject the VectorStore

    // Updated constructor to accept the VectorStore
    public ChatController(RagService ragService, ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.ragService = ragService;
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore; // Initialize it
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

    @PostMapping("/api/rag-chat")
    public ChatResponse ragChat(@RequestBody ChatRequest request) {
        String ragResponse = ragService.chatWithRag(request.message());
        return new ChatResponse(ragResponse);
    }

    /**
     * Debug endpoint to inspect the contents of the VectorStore.
     * Access this at http://localhost:8080/api/vector-store-contents
     * @return A list of all documents currently in the vector store.
     */
    @GetMapping("/api/vector-store-contents")
    public List<Document> getVectorStoreContents() {
        // This will perform a search for everything (up to 100 documents) and return it.
        return vectorStore.similaritySearch(SearchRequest.builder().query("").topK(10).build());
    }
}