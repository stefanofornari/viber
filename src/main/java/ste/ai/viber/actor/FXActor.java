package ste.ai.viber.actor;


import ste.ai.viber.renderer.Renderer;

import java.util.function.Consumer;
import org.json.JSONObject;
import ste.lazyjava.safe.Safe;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.ChatMessage;
import ste.llm.chat.model.Conversation;
import ste.llm.chat.model.ErrorMessage;
import ste.llm.chat.model.ReplyMessage;
import ste.llm.chat.model.ThoughtMessage;
import ste.llm.chat.model.ToolExecutionMessage;
import ste.llm.chat.model.ToolInvocationMessage;
import ste.llm.tools.InvocationJSONObject;

/**
 * Actor implementation for dev-mode manual message creation.
 *
 * <p>This actor does not interact with an LLM. Instead, it provides methods
 * to manually add messages to the current chat, making it suitable for
 * interactive testing and development.</p>
 */
public class FXActor implements Actor, Safe {

    private Conversation conversation;
    private Renderer renderer;
    private Chat currentChat;

    public FXActor(Conversation conversation, Renderer renderer) {
        requireNonNull(conversation, "conversation");
        this.conversation = conversation;
        this.renderer = renderer;
    }

    public void renderer(Renderer renderer) {
        requireNonNull(renderer, "renderer");
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

    public void addReply(final String text) {
        addMessage(new ReplyMessage(text));
    }

    public void addThought(final String text) {
        addMessage(new ThoughtMessage(text));
    }

    public void addToolInvocation(final String text) {
        try {
            JSONObject json = new JSONObject(text);
            String toolName = json.getString("tool");
            JSONObject arguments = json.getJSONObject("arguments");
            addMessage(new ToolInvocationMessage(new InvocationJSONObject(toolName, arguments)));
        } catch (Exception e) {
            addError("Invalid tool invocation JSON: " + e.getMessage());
        }
    }

    public void addToolReply(final String text) {
        addMessage(new ToolExecutionMessage(text));
    }

    public void addError(final String text) {
        addMessage(new ErrorMessage(text, null));
    }

    private void addMessage(final ChatMessage message) {
        if (currentChat == null) {
            throw new IllegalStateException("No active chat");
        }
        currentChat.addMessage(message);
        renderer.render(message);
    }
}
