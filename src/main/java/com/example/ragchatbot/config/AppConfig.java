package com.example.ragchatbot.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.ai.document.Document; // Import Document

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class AppConfig {

    @Value("classpath:/data/*.txt")
    private Resource[] resources;

    /**
     * This bean defines the chunking strategy for your documents.
     * It splits the text based on the number of tokens.
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter(
                // --- You can configure your chunking strategy here ---
                500,    // 1. Max chunk size in tokens
                100,    // 2. Chunk overlap in tokens
                5,      // 3. Min chunk size in characters
                1000,   // 4. Max chunk size in characters
                true    // 5. Break on nearest sentence
        );
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel, TokenTextSplitter tokenTextSplitter) {
        // Correctly initialize the VectorStore using the builder pattern
        SimpleVectorStore.SimpleVectorStoreBuilder builder = SimpleVectorStore.builder(embeddingModel);
        SimpleVectorStore vectorStore = builder.build();

        // 1. EXTRACT: Read all documents from the resources
        List<Document> allDocuments = new ArrayList<>();
        Arrays.stream(resources).forEach(resource -> {
            TextReader textReader = new TextReader(resource);
            textReader.getCustomMetadata().put("file_name", resource.getFilename());
            System.out.println("File added: " + resource.getFilename());
            allDocuments.addAll(textReader.get());
        });
        System.out.println("Extracted " + allDocuments.size() + " documents from resources.");

        // 2. TRANSFORM: Apply the chunking strategy to the documents
        List<Document> chunkedDocuments = tokenTextSplitter.apply(allDocuments);
        System.out.println("Transformed documents into " + chunkedDocuments.size() + " chunks.");

        // 3. LOAD: Add the final, chunked documents to the vector store
        vectorStore.add(chunkedDocuments);
        System.out.println("Vector Store initialized and " + chunkedDocuments.size() + " chunks loaded.");

        return vectorStore;
    }
}