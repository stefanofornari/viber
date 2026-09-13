package ste.ai.viber.renderer;

import ste.ai.viber.model.Chat;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.Message;
import ste.ai.viber.model.MessageType;
import ste.ai.viber.model.SystemMessage;

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
            for (Message message : chat.messages()) {
                renderMessageContent(message);
            }
            System.out.print(System.lineSeparator());
        }
    }

    @Override
    public void render(Chat chat) {
        requireNonNull(chat, "chat");
        renderChatHeader(1);
        for (Message message : chat.messages()) {
            renderMessageContent(message);
        }
        System.out.print(System.lineSeparator());
    }

    @Override
    public void render(Message message) {
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

    private void renderMessageContent(Message message) {
        System.out.print("  " +
            message.role().name() +
            "/" +
            message.type().name() +
            prefixFor(message.type()) +
            message.content() +
            System.lineSeparator());
    }

    private static String prefixFor(MessageType type) {
        return switch (type) {
            case PROMPT -> "> ";
            case REPLY -> ": ";
            case THOUGHT -> "~ ";
            case TOOL_EXECUTION_REQUEST -> "[TOOL] ";
            case TOOL_EXECUTION_RESPONSE -> "[OUT] ";
        };
    }

    private static void requireNonNull(Object obj, String name) {
        if (obj == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }
}
