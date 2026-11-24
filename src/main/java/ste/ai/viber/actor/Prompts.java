package ste.ai.viber.actor;

import ste.llm.chat.model.Chat;
import ste.llm.chat.model.ChatMessage;
import ste.llm.chat.model.PromptMessage;

/**
 * Helper to find the prompt an actor has to answer in a chat that may contain
 * more than one user prompt.
 */
final class Prompts {

    private Prompts() {
    }

    /**
     * Returns the most recent {@link PromptMessage} of the chat, or the chat's
     * first prompt if no prompt is found among its messages.
     */
    static PromptMessage latest(Chat chat) {
        PromptMessage latest = null;
        for (ChatMessage message : chat.messages()) {
            if (message instanceof PromptMessage prompt) {
                latest = prompt;
            }
        }
        return (latest != null) ? latest : chat.prompt();
    }
}
