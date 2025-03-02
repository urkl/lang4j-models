package net.urosk.llms.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import net.urosk.llms.AiSqlResponse;
import net.urosk.llms.AiSqlResponseExtractor;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
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
        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(userMessage));


        ChatLanguageModel chatModel = chatLanguageModelFactory.getModel(llmType);


        AiSqlResponse aiSqlResponse=null;
        String output = null;
        try {
            ChatResponse response = chatModel.chat(messages);


            AiSqlResponseExtractor aiSqlResponseExtractor = AiServices.create(AiSqlResponseExtractor.class, chatModel);
            aiSqlResponse = aiSqlResponseExtractor.extract(response.aiMessage().text());
            output = aiSqlResponse.getSql();

            saveToJsonFile(llmType,aiSqlResponse);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            output = "NAPAKA!  " + e.getMessage();

        }
        stopWatch.stop();
        long durationMillis = stopWatch.getTime();

        // Dodaj odgovor modela v MessageList (vključi tip modela in trajanje)
        assert aiSqlResponse != null;
        String botText = String.format("(%s, %d ms, %.8f USD): %s", llmType, durationMillis,aiSqlResponse.cost, output);

        return botText;

    }
    // Ustvari instanco ObjectMapper
    ObjectMapper objectMapper = new ObjectMapper();

    public void saveToJsonFile(LlmType llmType,AiSqlResponse aiSqlResponse) {


// Predpostavimo, da je aiSqlResponse že inicializiran
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("saved-prompts/"+llmType.name()+"-"+Instant.now().toEpochMilli()+"-aiSqlResponse.json"), aiSqlResponse);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
