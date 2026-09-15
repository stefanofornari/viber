package ste.ai.viber.model;

public sealed interface ChatMessage permits PromptMessage, ReplyMessage, ThoughtMessage, ToolExecutionRequestMessage, ToolExecutionResponseMessage {
    Role role();
    String content();
}
