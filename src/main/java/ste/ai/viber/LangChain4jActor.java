package ste.ai.viber;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import ste.ai.viber.actor.Actor;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.model.ReplyMessage;
import ste.ai.viber.model.SystemMessage;
import ste.ai.viber.model.ToolExecutionRequestMessage;
import ste.ai.viber.model.ToolExecutionResponseMessage;
import ste.ai.viber.tools.Tool;

import java.util.List;
import java.util.function.Consumer;
import static ste.ai.viber.Utils.ifNotNull;
import static ste.ai.viber.Utils.requireNonNull;

public class LangChain4jActor implements Actor {

    public interface ConversationConsumer extends Consumer {
        default void updated(final Conversation conversation) {
            accept(conversation);
        }
    }

    public interface ActorService {
        Result<String> chat(@UserMessage String userMessage);
    }

    private final ActorService service;
    private final Conversation conversation;
    private final ConversationConsumer updated;
    private final List<Tool> tools;

    public LangChain4jActor(ChatModel chatModel,
                            List<Tool> tools,
                            String systemPrompt,
                            ConversationConsumer updated) {
        requireNonNull(chatModel, "chatModel");
        requireNonNull(tools, "tools");
        requireNonNull(systemPrompt, "systemPrompt");
        if (systemPrompt.isBlank()) {
            throw new IllegalArgumentException("systemPrompt must not be blank");
        }
        if (tools.isEmpty()) {
            throw new IllegalArgumentException("tools must not be empty");
        }
        for (int i = 0; i < tools.size(); i++) {
            if (tools.get(i) == null) {
                throw new IllegalArgumentException("tools[%d] must not be null".formatted(i));
            }
        }

        this.conversation = new Conversation();
        this.updated = updated;
        this.tools = tools;

        conversation.systemMessage(new SystemMessage(systemPrompt));

        this.service = AiServices.builder(ActorService.class)
            .chatModel(chatModel)
            .tools(tools.toArray())
            .systemMessageProvider(o -> systemPrompt)
            .build();
    }

    public Conversation conversation() {
        return conversation;
    }

    public void chat(Chat chat) {
        requireNonNull(chat, "chat");

        if (!conversation.chats().contains(chat)) {
            conversation.addChat(chat);
        }
        update();

        PromptMessage prompt = chat.prompt();
        Result<String> result = service.chat(prompt.content());

        for (ToolExecution execution : result.toolExecutions()) {
            ToolExecutionRequest request = execution.request();
            String toolName = request.name();
            String arguments = request.arguments();

            String displayText = toolName;
            if ("input".equals(toolName)) {
                displayText = "ask for the name";
            } else if (arguments != null && !arguments.equals("{}")) {
                displayText = displayText + ": " + arguments;
            }

            chat.addMessage(new ToolExecutionRequestMessage(displayText));
            update();

            String toolResult = execution.result();
            chat.addMessage(new ToolExecutionResponseMessage(toolResult));
            update();
        }

        String finalReply = result.content();
        if (finalReply != null) {
            chat.addMessage(new ReplyMessage(finalReply));
            update();
        }
    }

    private void update() {
        ifNotNull(updated, () -> updated.updated(conversation));
    }
}
