package ste.ai.viber.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a chat: an ordered sequence of messages exchanged between a Viber and an Actor.
 */
public class Chat {
    private final List<Message> messages = new ArrayList<>();

    /**
     * Adds a message to this chat.
     *
     * @param message the message to add
     * @return this chat, for fluent usage
     */
    public Chat addMessage(Message message) {
        messages.add(message);
        return this;
    }

    /**
     * Returns an unmodifiable view of the messages in this chat.
     *
     * @return the messages
     */
    public List<Message> messages() {
        return Collections.unmodifiableList(messages);
    }
}
