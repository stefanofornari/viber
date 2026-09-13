package ste.ai.viber.model;

/**
 * Represents the type of a message exchanged in a conversation.
 */
public enum MessageType {
    /**
     * A message initiating a request, usually text or image.
     */
    PROMPT,
    /**
     * A response message, usually text or markdown.
     */
    REPLY,
    /**
     * An internal thought or reasoning message, usually text.
     */
    THOUGHT,
    /**
     * A request to execute a tool, containing the tool's name and arguments.
     */
    TOOL_EXECUTION_REQUEST,
    /**
     * The output returned from a tool execution.
     */
    TOOL_EXECUTION_RESPONSE
}
