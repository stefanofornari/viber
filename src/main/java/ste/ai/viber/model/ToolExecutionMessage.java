package ste.ai.viber.model;

public record ToolExecutionMessage(String content) implements ChatMessage {
    @Override
    public Role role() {
        return Role.VIBER;
    }
}
