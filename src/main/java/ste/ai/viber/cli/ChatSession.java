package ste.ai.viber.cli;

import ste.llm.chat.model.Chat;
import ste.llm.chat.model.PromptMessage;

/**
 * Keeps track of the chat the user is currently working on: every prompt goes
 * to the same chat until the session is {@link #reset() reset} (e.g. when the
 * chat is archived), after which the next prompt starts a new chat.
 *
 * <p>Shared by the main flow and the dev tool so that both follow the same
 * chat logic.</p>
 */
public class ChatSession {

    /**
     * The outcome of a prompt.
     *
     * @param chat the chat the prompt belongs to
     * @param prompt the prompt message
     * @param newChat true if the prompt started a new chat
     */
    public record Turn(Chat chat, PromptMessage prompt, boolean newChat) {
    }

    private Chat current;

    /**
     * Adds a prompt to the current chat, starting a new chat if there is none.
     */
    public Turn prompt(String text) {
        PromptMessage prompt = new PromptMessage(text);
        if (current == null) {
            current = new Chat(prompt);
            return new Turn(current, prompt, true);
        }
        current.addMessage(prompt);
        return new Turn(current, prompt, false);
    }

    public boolean isCurrent(Chat chat) {
        return chat != null && chat == current;
    }

    public void reset() {
        current = null;
    }
}
