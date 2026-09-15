package ste.ai.viber.cli;

import ste.ai.viber.actor.Actor;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.renderer.Renderer;
import ste.ai.viber.renderer.StringRenderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class VibeChatCLI {
    private final Actor actor;
    private final Conversation conversation;
    private final Renderer renderer;
    private final Reader input;

    public VibeChatCLI(Actor actor, Conversation conversation) {
        this(actor, conversation, new StringRenderer(), new InputStreamReader(System.in));
    }

    public VibeChatCLI(Actor actor) {
        this(actor, actor.conversation(), new StringRenderer(), new InputStreamReader(System.in));
    }

    public VibeChatCLI(Actor actor, Conversation conversation, Renderer renderer, Reader input) {
        this.actor = actor;
        this.conversation = conversation;
        this.renderer = renderer;
        this.input = input;
    }

    public void start() {
        BufferedReader reader = new BufferedReader(input);
        renderConversation();

        String line;
        while (true) {
            try {
                line = reader.readLine();
            } catch (IOException e) {
                return;
            }
            if (line == null) {
                return;
            }
            if (isExitCommand(line)) {
                break;
            }
            if (line.isBlank()) {
                continue;
            }

            ste.ai.viber.model.Chat chat = new ste.ai.viber.model.Chat(new ste.ai.viber.model.PromptMessage(line));
            actor.chat(chat);
            renderConversation();
        }
    }

    private boolean isExitCommand(String line) {
        return "/exit".equalsIgnoreCase(line.trim());
    }

    private void renderConversation() {
        renderer.render(conversation);
    }
}
