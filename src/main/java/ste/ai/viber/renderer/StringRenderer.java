package ste.ai.viber.renderer;

import ste.llm.chat.model.Chat;
import ste.llm.chat.model.ChatMessage;
import ste.llm.chat.model.Conversation;
import ste.llm.chat.model.ErrorMessage;
import ste.llm.chat.model.PromptMessage;
import ste.llm.chat.model.ReplyMessage;
import ste.llm.chat.model.SystemMessage;
import ste.llm.chat.model.ThoughtMessage;
import ste.llm.chat.model.ToolExecutionMessage;
import ste.llm.chat.model.ToolInvocationMessage;



/**
 * Plain-text {@link Renderer} that writes rendered output directly to {@link System#out}.
 */
public class StringRenderer implements Renderer {

    @Override
    public void render(Conversation conversation) {
        requireNonNull(conversation, "conversation");
        if (conversation.chats().isEmpty()) {
            System.out.print("(empty conversation)" + System.lineSeparator());
            return;
        }

        conversation.systemMessage().ifPresent(this::renderSystemMessage);

        int index = 1;
        for (Chat chat : conversation.chats()) {
            renderChatHeader(index++);
            for (ChatMessage message : chat.messages()) {
                renderMessageContent(message);
            }
            System.out.print(System.lineSeparator());
        }
    }

    @Override
    public void render(Chat chat) {
        requireNonNull(chat, "chat");
        renderChatHeader(1);
        for (ChatMessage message : chat.messages()) {
            renderMessageContent(message);
        }
        System.out.print(System.lineSeparator());
    }

    @Override
    public void render(ChatMessage message) {
        requireNonNull(message, "message");
        renderMessageContent(message);
    }

    private void renderChatHeader(int index) {
        System.out.print("--- Chat " + index + " ---" + System.lineSeparator());
    }

    private void renderSystemMessage(SystemMessage systemMessage) {
        System.out.print("--- System ---" + System.lineSeparator());
        System.out.print("  " + systemMessage.content() + System.lineSeparator());
    }

    private void renderMessageContent(ChatMessage message) {
        System.out.print("  " +
            message.role().name() +
            "/" +
            typeNameFor(message) +
            prefixFor(message) +
            message.content() +
            System.lineSeparator());
    }

    private static String typeNameFor(ChatMessage message) {
        return switch (message) {
            case PromptMessage ignored -> "PROMPT";
            case ReplyMessage ignored -> "REPLY";
            case ThoughtMessage ignored -> "THOUGHT";
            case ToolInvocationMessage ignored -> "TOOL_EXECUTION_REQUEST";
            case ToolExecutionMessage ignored -> "TOOL_EXECUTION_RESPONSE";
            case ErrorMessage ignored -> "ERROR";
        };
    }

    private static String prefixFor(ChatMessage message) {
        return switch (message) {
            case PromptMessage ignored -> "> ";
            case ReplyMessage ignored -> ": ";
            case ThoughtMessage ignored -> "~ ";
            case ToolInvocationMessage ignored -> "[TOOL] ";
            case ToolExecutionMessage ignored -> "[OUT] ";
            case ErrorMessage ignored -> "[ERROR] ";
        };
    }

    private static void requireNonNull(Object obj, String name) {
        if (obj == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }
}
