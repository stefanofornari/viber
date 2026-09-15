package ste.ai.viber.renderer;

import ste.ai.viber.model.Chat;
import ste.ai.viber.model.ChatMessage;
import ste.ai.viber.model.Conversation;

/**
 * Renders the content of a Conversation, Chat, or ChatMessage.
 */
public interface Renderer {

    /**
     * Renders the given conversation.
     *
     * @param conversation the conversation to render
     * @throws IllegalArgumentException if conversation is null
     */
    void render(Conversation conversation);

    /**
     * Renders the given chat.
     *
     * @param chat the chat to render
     * @throws IllegalArgumentException if chat is null
     */
    void render(Chat chat);

    /**
     * Renders the given message.
     *
     * @param message the message to render
     * @throws IllegalArgumentException if message is null
     */
    void render(ChatMessage message);
}
