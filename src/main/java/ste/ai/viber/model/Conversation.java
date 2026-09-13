package ste.ai.viber.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a conversation: an ordered sequence of chats between a Viber and an Actor.
 */
public class Conversation {
    private final List<Chat> chats = new ArrayList<>();
    private SystemMessage systemMessage;

    /**
     * Creates a new empty Conversation.
     */
    public Conversation() {
    }

    /**
     * Adds a chat to this conversation.
     *
     * @param chat the chat to add
     * @return this conversation, for fluent usage
     */
    public Conversation addChat(Chat chat) {
        chats.add(chat);
        return this;
    }

    /**
     * Returns an unmodifiable view of the chats in this conversation.
     *
     * @return the chats
     */
    public List<Chat> chats() {
        return Collections.unmodifiableList(chats);
    }

    /**
     * Sets the system message for this conversation.
     *
     * @param systemMessage the system message
     * @return this conversation, for fluent usage
     */
    public Conversation systemMessage(SystemMessage systemMessage) {
        this.systemMessage = systemMessage;
        return this;
    }

    /**
     * Returns the system message for this conversation, if present.
     *
     * @return an Optional containing the system message, or empty if none is set
     */
    public Optional<SystemMessage> systemMessage() {
        return Optional.ofNullable(systemMessage);
    }
}
