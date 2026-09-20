package ste.ai.viber.cli;

import dev.langchain4j.exception.AuthenticationException;
import dev.langchain4j.exception.ModelNotFoundException;
import dev.langchain4j.exception.RateLimitException;
import ste.ai.viber.actor.Actor;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.model.ReplyMessage;
import ste.ai.viber.model.ErrorMessage;
import ste.ai.viber.renderer.Renderer;
import ste.ai.viber.renderer.StringRenderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViberCLI {
    private static final Logger LOG = Logger.getLogger(ViberCLI.class.getName());

    private final Actor actor;
    private final Conversation conversation;
    private final Renderer renderer;
    private final Reader input;

    public ViberCLI(Actor actor, Conversation conversation) {
        this(actor, conversation, new StringRenderer(), new InputStreamReader(System.in));
    }

    public ViberCLI(Actor actor) {
        this(actor, actor.conversation(), new StringRenderer(), new InputStreamReader(System.in));
    }

    public ViberCLI(Actor actor, Conversation conversation, Renderer renderer, Reader input) {
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

            final Chat chat = new Chat(new PromptMessage(line));
            conversation.addChat(chat);
            try {
                actor.chat(chat, msg -> renderConversation());
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, "Chat failed", e);
                final Throwable cause = e.getCause();
                switch (cause) {
                    case AuthenticationException x -> chat.addMessage(new ErrorMessage(
                        "Authentication failed: " + x.getMessage(),
                        x
                    ));
                    case RateLimitException x -> chat.addMessage(new ErrorMessage(
                        "Rate limit exceeded: " + x.getMessage(),
                        x
                    ));
                    case ModelNotFoundException x -> chat.addMessage(new ErrorMessage(
                        "Invalid model name: " + x.getMessage(),
                        x
                    ));
                    default -> {
                    }
                }
                chat.addMessage(new ReplyMessage("Error: " + e.getMessage()));
                renderConversation();
            }
        }
    }

    private boolean isExitCommand(String line) {
        return "/exit".equalsIgnoreCase(line.trim());
    }

    private void renderConversation() {
        renderer.render(conversation);
    }
}
