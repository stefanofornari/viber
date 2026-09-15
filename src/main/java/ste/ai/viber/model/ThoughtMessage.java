package ste.ai.viber.model;

public record ThoughtMessage(String content) implements ChatMessage {
    @Override
    public Role role() {
        return Role.ACTOR;
    }
}
