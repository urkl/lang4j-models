package net.urosk.llms.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import dev.langchain4j.model.chat.ChatLanguageModel;


@Service
public class ChatLanguageModelFactory {

    private final ChatLanguageModel openAiModel;
    private final ChatLanguageModel geminiModel;
    private final ChatLanguageModel mistralModel;
    private final ChatLanguageModel perplexityModel;

    @Autowired
    public ChatLanguageModelFactory(
            ChatLanguageModel openAiModel,
            ChatLanguageModel geminiModel,
            ChatLanguageModel mistralModel,
            ChatLanguageModel perplexityModel) {
        this.openAiModel = openAiModel;
        this.geminiModel = geminiModel;
        this.mistralModel = mistralModel;
        this.perplexityModel = perplexityModel;
    }

    public ChatLanguageModel getModel(LlmType type) {
        return switch (type) {
            case OPENAI -> openAiModel;
            case GEMINI -> geminiModel;
            case MISTRAL -> mistralModel;
            case PERPLEXITY -> perplexityModel;
            default -> throw new IllegalArgumentException("Neveljaven tip LLM: " + type);
        };
    }
}