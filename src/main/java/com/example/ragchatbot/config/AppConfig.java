package com.example.ragchatbot.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class AppConfig {

    @Value("classpath:/data/company_policy.txt")
    private Resource resource;

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // 1. Get the builder and pass the EmbeddingModel
        SimpleVectorStore.SimpleVectorStoreBuilder builder = SimpleVectorStore.builder(embeddingModel);

        // 2. Build the SimpleVectorStore instance
        SimpleVectorStore vectorStore = builder.build();

        // 3. Load your documents into the store
        TextReader textReader = new TextReader(resource);
        textReader.getCustomMetadata().put("file_name", "company_policy.txt");
        vectorStore.add(textReader.get());

        System.out.println("Vector Store initialized and documents loaded.");

        return vectorStore;
    }
}