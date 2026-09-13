package ste.ai.viber;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.Message;
import ste.ai.viber.model.MessageType;
import ste.ai.viber.model.Role;
import ste.ai.viber.model.SystemMessage;

import java.util.List;

public class LangChain4jActor {

    public interface ConversationUpdateListener {
        void conversationUpdated(Conversation conversation);
    }

    private interface ActorService {
        Result<String> chat(@UserMessage String userMessage);
    }

    private final ActorService service;
    private final Conversation conversation;
    private final ConversationUpdateListener listener;
    private final List<Object> tools;

    public LangChain4jActor(ChatModel chatModel,
                            List<Object> tools,
                            String systemPrompt,
                            ConversationUpdateListener listener) {
        this.conversation = new Conversation();
        this.listener = listener;
        this.tools = tools;

        Chat chat = new Chat();
        conversation.addChat(chat);
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

    public void chat(String userMessage) {
        Chat chat = conversation.chats().get(0);
        chat.addMessage(new Message(Role.VIBER, MessageType.PROMPT, userMessage));
        fireConversationUpdated();

        Result<String> result = service.chat(userMessage);

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

            chat.addMessage(new Message(Role.ACTOR, MessageType.TOOL_EXECUTION_REQUEST, displayText));
            fireConversationUpdated();

            String toolResult = execution.result();
            chat.addMessage(new Message(Role.VIBER, MessageType.TOOL_EXECUTION_RESPONSE, toolResult));
            fireConversationUpdated();
        }

        String finalReply = result.content();
        if (finalReply != null) {
            chat.addMessage(new Message(Role.ACTOR, MessageType.REPLY, finalReply));
            fireConversationUpdated();
        }
    }

    private void fireConversationUpdated() {
        if (listener != null) {
            listener.conversationUpdated(conversation);
        }
    }
}
