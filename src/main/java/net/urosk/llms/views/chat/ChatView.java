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
import dev.langchain4j.data.message.SystemMessage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.urosk.llms.services.ChatService;
import net.urosk.llms.services.LlmType;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Chat Client")
@Route("")
@Slf4j
public class ChatView extends VerticalLayout {

    private final MessageList messageList;
    private final TextArea inputField;
    private final Button sendButton;
    private final Button sendToAllButton;

    List<MessageListItem> messages = new ArrayList<>();
    RadioButtonGroup<LlmType> llmTypeRadioGroup = new RadioButtonGroup<>();
    SystemMessage systemPrompt;
    @Autowired
    ChatService chatService;
    // Naložimo datoteko s sistemskim promptom iz resources
    @Value("classpath:system_prompt.txt")
    private Resource systemPromptResource;

    public ChatView() {

        setSizeFull();
        addClassName("chat-client-view");


        // RadioButtonGroup za izbiro modela

        llmTypeRadioGroup.setLabel("Izberi model jezika");
        llmTypeRadioGroup.setItems(LlmType.values());
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

        inputField.setValue("Pripravi podatke o številu in vrednosti predmetov glede na odgovorno osebo, kjer je vnešena računovodska vrednost večja od 10 EUR.");
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


            String out = chatService.sendMessage(type, userMessage, systemPrompt.text());


            MessageListItem botItem = new MessageListItem(out, Instant.now(), type.name());

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

        // Dodaj uporabniško sporočilo v MessageList
        MessageListItem userItem = new MessageListItem(userMessage, Instant.now(), "Uroš");
        messages.add(userItem);


        // Počisti vnosno polje
        inputField.clear();

        String out = chatService.sendMessage(llmTypeRadioGroup.getValue(), userMessage, systemPrompt.text());


        MessageListItem botItem = new MessageListItem(out, Instant.now(), llmTypeRadioGroup.getValue().name());


        messages.add(botItem);

        messageList.setItems(messages);

    }

    @PostConstruct
    private void loadSystemPrompt() {
        try {

            systemPrompt = SystemMessage.from(new String(systemPromptResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8));

        } catch (IOException e) {
            log.error("Didn't find system prompt",e);

        }
    }
}
