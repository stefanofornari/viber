package ste.ai.viber.model;

public record ToolInvocationMessage(String content) implements ChatMessage {
    @Override
    public Role role() {
        return Role.ACTOR;
    }
}
