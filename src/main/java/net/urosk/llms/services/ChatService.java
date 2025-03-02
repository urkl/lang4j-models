package net.urosk.llms.services;


import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ChatService {

    @Autowired
    private ChatLanguageModelFactory chatLanguageModelFactory;

    public String sendMessage(LlmType llmType, String userMessage, String systemPrompt) {

        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Vnesite sporočilo!";
        }
        StopWatch stopWatch = StopWatch.createStarted();

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new UserMessage(userMessage));
        messages.add(new SystemMessage(systemPrompt));

        ChatLanguageModel chatModel = chatLanguageModelFactory.getModel(llmType);

        String output = "";
        try {
            ChatResponse response = chatModel.chat(messages);
            output = response.aiMessage().text();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            output = "NAPAKA!  " + e.getMessage();

        }
        stopWatch.stop();
        long durationMillis = stopWatch.getTime();

        // Dodaj odgovor modela v MessageList (vključi tip modela in trajanje)
        String botText = String.format("(%s, %d ms): %s", llmType, durationMillis, output);

        return botText;

    }
}
