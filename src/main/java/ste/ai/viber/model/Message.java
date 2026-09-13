package ste.ai.viber.model;

/**
 * Represents a single message exchanged between a Viber and an Actor.
 *
 * @param role the sender role
 * @param type the message type
 * @param content the message content
 */
public record Message(Role role, MessageType type, String content) {
}
