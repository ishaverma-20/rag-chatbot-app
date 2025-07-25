package com.example.ragchatbot.service;

import com.example.ragchatbot.controller.DataController;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final DataController dataController;

    @Value("classpath:/prompts/rag-prompt.st")
    private Resource ragPromptResource;

    public RagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, DataController dataController) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
        this.dataController = dataController;
    }

    public String chatWithRag(String userMessage) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);

        // --- UPDATED: Added a similarity threshold to the search request ---
        // This value is between 0 and 1. A higher value means a stricter match is required.
        // You can tune this value to fit your needs. 0.75 is a good starting point.
        SearchRequest request = SearchRequest.builder()
                .query(userMessage)
                .topK(3)
                .similarityThreshold(0.6)
                .build();

        List<Document> relevantDocuments = this.vectorStore.similaritySearch(request);

        // --- NEW: Check if any relevant documents were found ---
        // If the list is empty, it means no documents met the 0.75 similarity threshold.
        if (relevantDocuments.isEmpty()) {
            System.out.println("No relevant documents found for the query: " + userMessage);
            return "I do not have enough information regarding that topic to provide an answer.";
        }

        String context = relevantDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        if (isLoggedIn && isDataRelatedQuery(userMessage)) {
            System.out.println("User is logged in and query is data-related. Augmenting context.");
//            context += "\n\n--- Orders Data ---\n" + dataController.getOrders().toPrettyString();
            context += "\n\n--- Transactions Stats ---\n" + dataController.getTransactionStats();
        } else {
            System.out.println("Using public context only.");
        }

        System.out.println("--- Final Context for LLM ---");
        System.out.println(context);
        System.out.println("-----------------------------");

        PromptTemplate promptTemplate = new PromptTemplate(this.ragPromptResource);
        Map<String, Object> promptValues = Map.of("context", context, "question", userMessage);

        return chatClient.prompt()
                .messages(promptTemplate.createMessage(promptValues))
                .call()
                .content();
    }

    private boolean isDataRelatedQuery(String message) {
        String lowerCaseMessage = message.toLowerCase();
        return lowerCaseMessage.contains("order") ||
                lowerCaseMessage.contains("transaction") ||
                lowerCaseMessage.contains("stats") ||
                lowerCaseMessage.contains("merchant");
    }
}