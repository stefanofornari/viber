package ste.ai.viber.model;

public record ToolExecutionRequestMessage(String content) implements ChatMessage {
    @Override
    public Role role() {
        return Role.ACTOR;
    }
}
