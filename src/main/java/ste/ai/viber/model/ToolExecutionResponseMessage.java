package ste.ai.viber.model;

public record ToolExecutionResponseMessage(String content) implements ChatMessage {
    @Override
    public Role role() {
        return Role.VIBER;
    }
}
