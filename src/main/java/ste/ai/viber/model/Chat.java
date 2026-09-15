package ste.ai.viber.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static ste.ai.viber.Utils.requireNonNull;

public class Chat {
    private final PromptMessage prompt;
    private final List<ChatMessage> messages = new ArrayList<>();

    public Chat(PromptMessage prompt) {
        requireNonNull(prompt, "prompt");
        this.prompt = prompt;
        messages.add(prompt);
    }

    public PromptMessage prompt() {
        return prompt;
    }

    public Chat addMessage(ChatMessage message) {
        messages.add(message);
        return this;
    }

    public List<ChatMessage> messages() {
        return Collections.unmodifiableList(messages);
    }
}
