package com.recruiting.platform.orchestration.ai;

import com.recruiting.platform.orchestration.config.AiPlatformProperties;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class AiModelFactory {

    private final AiPlatformProperties properties;
    private volatile ChatModel chatModel;

    public AiModelFactory(AiPlatformProperties properties) {
        this.properties = properties;
    }

    public Optional<ChatModel> chatModel() {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            return Optional.empty();
        }
        if (chatModel == null) {
            synchronized (this) {
                if (chatModel == null) {
                    chatModel = OpenAiChatModel.builder()
                            .apiKey(properties.getApiKey())
                            .baseUrl(properties.getBaseUrl())
                            .modelName(properties.getModelName())
                            .temperature(properties.getTemperature())
                            .timeout(Duration.ofSeconds(30))
                            .strictJsonSchema(true)
                            .build();
                }
            }
        }
        return Optional.of(chatModel);
    }
}
