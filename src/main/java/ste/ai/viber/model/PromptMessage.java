package ste.ai.viber.model;

import static ste.ai.viber.util.Safe.requireNonBlank;

public record PromptMessage(String content) implements ChatMessage {
    public PromptMessage {
        requireNonBlank(content, "content");
    }

    @Override
    public Role role() {
        return Role.VIBER;
    }
}
