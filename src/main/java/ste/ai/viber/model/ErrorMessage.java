package ste.ai.viber.model;

public record ErrorMessage(String content, Throwable cause) implements ChatMessage {
    @Override
    public Role role() {
        return Role.ACTOR;
    }
}
