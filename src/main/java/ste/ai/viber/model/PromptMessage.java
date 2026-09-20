package ste.ai.viber.model;

import static ste.ai.viber.util.Utils.requireNonBlank;

public record PromptMessage(String content) implements ChatMessage {
    public PromptMessage {
        requireNonBlank(content, "content");
    }

    @Override
    public Role role() {
        return Role.VIBER;
    }
}
