package net.urosk.llms.configurations;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModelName;
import dev.langchain4j.model.mistralai.internal.api.MistralAiResponseFormatType;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelsConfig {

    @Bean("openAiModel")
    public ChatLanguageModel openAiModel(
            @Value("${langchain4j.open-ai.chat-model.api-key}") String apiKey,
            @Value("${langchain4j.open-ai.chat-model.model-name}") String modelName) {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(OpenAiChatModelName.valueOf(modelName))
                .logRequests(true)
                .logResponses(true)
                .responseFormat("json")
                .build();
    }

    @Bean("geminiModel")
    public ChatLanguageModel geminiModel(
            @Value("${langchain4j.gemini.chat-model.api-key}") String apiKey,
            @Value("${langchain4j.gemini.chat-model.model-name}") String modelName) {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .logRequestsAndResponses(true)
                .responseFormat(ResponseFormat.JSON)
                .build();
    }

    @Bean("mistralModel")
    public ChatLanguageModel mistralModel(
            @Value("${langchain4j.mistral-ai.chat-model.api-key}") String apiKey,
            @Value("${langchain4j.mistral-ai.chat-model.model-name}") String modelName) {
        // Predpostavimo, da je modelName enak vrednosti iz enum-a MistralAiChatModelName (npr. MISTRAL_SMALL_LATEST)
        return MistralAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(MistralAiChatModelName.valueOf(modelName))
                .logRequests(true)
                .logResponses(true)
                .responseFormat(MistralAiResponseFormatType.JSON_OBJECT)
                .build();
    }

    @Bean("perplexityModel")
    public ChatLanguageModel perplexityModel(
            @Value("${langchain4j.perplexity.chat-model.api-key}") String apiKey,
            @Value("${langchain4j.perplexity.chat-model.model-name}") String modelName,
            @Value("${langchain4j.perplexity.chat-model.base-url}") String baseUrl) {
        // Perplexity API je OpenAI-kompatibilen, zato uporabimo isti builder, a nastavimo tudi baseUrl
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .logRequests(true)
                .logResponses(true)
                .responseFormat("json")
                .build();
    }
}
