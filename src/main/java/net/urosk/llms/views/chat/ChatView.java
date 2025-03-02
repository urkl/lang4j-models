package net.urosk.llms.views.chat;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import dev.ai4j.openai4j.chat.Message;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.PostConstruct;
import net.urosk.llms.services.ChatLanguageModelFactory;
import net.urosk.llms.services.LlmType;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Chat Client")
@Route("")
public class ChatView extends VerticalLayout {

    private final MessageList messageList;
    private final TextArea inputField;
    private final Button sendButton;
    private final Button sendToAllButton;
    private final ChatLanguageModelFactory chatLanguageModelFactory;
    List<MessageListItem> messages = new ArrayList<>();
    RadioButtonGroup<LlmType> llmTypeRadioGroup = new RadioButtonGroup<>();
    SystemMessage systemPrompt;
    private ChatLanguageModel chatModel;
    // Naložimo datoteko s sistemskim promptom iz resources
    @Value("classpath:system_prompt.txt")
    private Resource systemPromptResource;

    public ChatView(ChatLanguageModelFactory chatLanguageModelFactory) {
        this.chatLanguageModelFactory = chatLanguageModelFactory;
        setSizeFull();
        addClassName("chat-client-view");

        // RadioButtonGroup za izbiro modela

        llmTypeRadioGroup.setLabel("Izberi model jezika");
        llmTypeRadioGroup.setItems(LlmType.values());
        llmTypeRadioGroup.addValueChangeListener(event -> {
            chatModel = chatLanguageModelFactory.getModel(event.getValue());
        });
        llmTypeRadioGroup.setValue(LlmType.OPENAI);

        // Ustvarimo MessageList za prikaz sporočil
        messageList = new MessageList();
        messageList.setWidthFull();
        messageList.getStyle().set("overflow-y", "auto");
        messageList.getStyle().set("padding", "10px");

        // Vnosno polje za sporočila
        inputField = new TextArea();
        inputField.setPlaceholder("Vnesite sporočilo...");
        inputField.setWidthFull();
        inputField.setHeight("100px");

        // Gumb za pošiljanje sporočila posameznemu modelu
        sendButton = new Button("Pošlji", event -> sendMessage());
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Gumb za pošiljanje sporočila vsem modelom
        sendToAllButton = new Button("Pošlji vsem", event -> sendToAllMessage());
        sendToAllButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout inputLayout = new HorizontalLayout(inputField, sendButton, sendToAllButton);
        inputLayout.setWidthFull();
        inputLayout.setDefaultVerticalComponentAlignment(Alignment.END);

        add(llmTypeRadioGroup, messageList, inputLayout);
        setFlexGrow(1, messageList);

        messageList.setItems(messages);
    }

    private void sendToAllMessage() {


        String userMessage = inputField.getValue();
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return;
        }

        // Dodaj uporabniško sporočilo v MessageList
        MessageListItem userItem = new MessageListItem(userMessage, Instant.now(), "Uroš");
        messages.add(userItem);

        // Počisti vnosno polje
        inputField.clear();

        // Pošlji sporočilo vsem modelom in izmeri trajanje klica
        for (LlmType type : LlmType.values()) {
            StopWatch stopWatch = StopWatch.createStarted();



            ChatResponse response = chatModel.chat( List.of(systemPrompt, UserMessage.from(userMessage)));

            stopWatch.stop();
            long durationMillis = stopWatch.getTime();

            // Dodaj odgovor modela v MessageList (vključi tip modela in trajanje)
            String botText = String.format("(%s, %d ms): %s", type.name(), durationMillis, response.aiMessage().text());
            MessageListItem botItem = new MessageListItem(botText, Instant.now(), type.name());

            messages.add(botItem);
        }

        // Dodaj sporočila v MessageList
        messageList.setItems(messages);

    }

    private void sendMessage() {
        String userMessage = inputField.getValue();
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return;
        }
        StopWatch stopWatch = StopWatch.createStarted();
        // Dodaj uporabniško sporočilo v MessageList
        MessageListItem userItem = new MessageListItem(userMessage, Instant.now(), "Uroš");
        messages.add(userItem);


        // Počisti vnosno polje
        inputField.clear();

        // Pošlji sporočilo izbranemu modelu in pridobi odgovor

        ChatResponse response = chatModel.chat( List.of(systemPrompt, UserMessage.from(userMessage)));
        stopWatch.stop();
        long durationMillis = stopWatch.getTime();

        // Dodaj odgovor modela v MessageList (vključi tip modela in trajanje)
        String botText = String.format("(%s, %d ms): %s", llmTypeRadioGroup.getValue().name(), durationMillis, response.aiMessage().text());
        // Dodaj odgovor modela v MessageList
        MessageListItem botItem = new MessageListItem(botText, Instant.now(), llmTypeRadioGroup.getValue().name());


        messages.add(botItem);

        messageList.setItems(messages);

    }

    @PostConstruct
    private void loadSystemPrompt() {
        try {

            systemPrompt = SystemMessage.from(new String(systemPromptResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8));

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
