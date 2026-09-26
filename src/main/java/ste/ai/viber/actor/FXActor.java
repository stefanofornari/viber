package ste.ai.viber.actor;

import javafx.application.Platform;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.ChatMessage;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.ErrorMessage;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.model.ReplyMessage;
import ste.ai.viber.model.ThoughtMessage;
import ste.ai.viber.model.ToolExecutionRequestMessage;
import ste.ai.viber.model.ToolExecutionResponseMessage;
import ste.ai.viber.renderer.Renderer;

import java.util.function.Consumer;

import static ste.ai.viber.util.Safe.requireNonNull;

/**
 * Actor implementation for dev-mode manual message creation.
 *
 * <p>This actor does not interact with an LLM. Instead, it provides methods
 * to manually add messages to the current chat, making it suitable for
 * interactive testing and development.</p>
 */
public class FXActor implements Actor {

    private final Conversation conversation;
    private final Renderer renderer;
    private Chat currentChat;

    public FXActor(Conversation conversation, Renderer renderer) {
        requireNonNull(conversation, "conversation");
        requireNonNull(renderer, "renderer");
        this.conversation = conversation;
        this.renderer = renderer;
    }

    @Override
    public void chat(Chat chat, Consumer<ChatMessage> onMessage) {
        requireNonNull(chat, "chat");
        requireNonNull(onMessage, "onMessage");
        conversation.addChat(chat);
        currentChat = chat;
        renderer.render(conversation);
    }

    @Override
    public Conversation conversation() {
        return conversation;
    }

    public Chat currentChat() {
        return currentChat;
    }

    public void addReply(String text) {
        addMessage(new ReplyMessage(text));
    }

    public void addThought(String text) {
        addMessage(new ThoughtMessage(text));
    }

    public void addToolExecution(String text) {
        addMessage(new ToolExecutionRequestMessage(text));
    }

    public void addToolReply(String text) {
        addMessage(new ToolExecutionResponseMessage(text));
    }

    public void addError(String text) {
        addMessage(new ErrorMessage(text, null));
    }

    private void addMessage(ChatMessage message) {
        if (currentChat == null) {
            throw new IllegalStateException("No active chat");
        }
        currentChat.addMessage(message);
        renderer.render(message);
    }
}
