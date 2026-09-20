package ste.ai.viber.model;

public sealed interface ChatMessage permits PromptMessage, ReplyMessage, ThoughtMessage, ToolExecutionRequestMessage, ToolExecutionResponseMessage, ErrorMessage {
    Role role();
    String content();
    default String contentType() {
        return "text/plain";
    }
}
