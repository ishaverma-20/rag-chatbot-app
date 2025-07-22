package com.example.ragchatbot.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Value("classpath:/prompts/rag-prompt.st")
    private Resource ragPromptResource;

    public RagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    public String chatWithRag(String userMessage) {
        SearchRequest request = SearchRequest.builder()
                .query(userMessage)
                .topK(2)
                .build();
        List<Document> relevantDocuments = this.vectorStore.similaritySearch(request);

        // Corrected: Use document.getText() to get the document's content
        String context = relevantDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        // Add these lines to inspect the context
        System.out.println("--- Retrieved Context ---");
        System.out.println(context);
        System.out.println("-------------------------");

        PromptTemplate promptTemplate = new PromptTemplate(this.ragPromptResource);
        Map<String, Object> promptValues = Map.of("context", context, "question", userMessage);

        return chatClient.prompt()
                .messages(promptTemplate.createMessage(promptValues))
                .call()
                .content();
    }

    public void loadDocumentsForRag(List<String> documentContents) {
        System.out.println("Loading documents for RAG (placeholder): " + documentContents.size() + " documents");
    }
}