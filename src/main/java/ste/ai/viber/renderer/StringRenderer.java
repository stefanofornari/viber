package ste.ai.viber.renderer;

import ste.ai.viber.model.Chat;
import ste.ai.viber.model.ChatMessage;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.model.ReplyMessage;
import ste.ai.viber.model.ThoughtMessage;
import ste.ai.viber.model.ToolExecutionRequestMessage;
import ste.ai.viber.model.ToolExecutionResponseMessage;

import java.io.IOException;
import java.io.UncheckedIOException;

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

    private void renderSystemMessage(ste.ai.viber.model.SystemMessage systemMessage) {
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
            case ToolExecutionRequestMessage ignored -> "TOOL_EXECUTION_REQUEST";
            case ToolExecutionResponseMessage ignored -> "TOOL_EXECUTION_RESPONSE";
        };
    }

    private static String prefixFor(ChatMessage message) {
        return switch (message) {
            case PromptMessage ignored -> "> ";
            case ReplyMessage ignored -> ": ";
            case ThoughtMessage ignored -> "~ ";
            case ToolExecutionRequestMessage ignored -> "[TOOL] ";
            case ToolExecutionResponseMessage ignored -> "[OUT] ";
        };
    }

    private static void requireNonNull(Object obj, String name) {
        if (obj == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }
}
