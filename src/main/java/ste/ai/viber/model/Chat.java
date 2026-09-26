package ste.ai.viber.model;

import dev.langchain4j.model.output.TokenUsage;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import static ste.ai.viber.util.Safe.requireNonNull;

public class Chat {
    private final PromptMessage prompt;
    private final List<ChatMessage> messages = new CopyOnWriteArrayList<>();
    private TokenUsage tokenUsage;

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

    public TokenUsage tokenUsage() {
        return tokenUsage;
    }

    public void tokenUsage(TokenUsage tokenUsage) {
        this.tokenUsage = tokenUsage;
    }
}
