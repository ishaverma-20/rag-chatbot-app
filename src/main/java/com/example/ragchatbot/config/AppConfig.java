package com.example.ragchatbot.config;

// Removed: import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    // Spring AI's ChatClient (Ollama implementation) is typically auto-configured when
    // spring-ai-starter-ollama is on the classpath and Ollama is running.
    // You can inject ChatClient.Builder directly into your services/controllers.
}