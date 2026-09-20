package ste.ai.viber.cli;

import ste.ai.viber.log.LogViewerWindow;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import picocli.CommandLine;
import ste.ai.viber.LangChain4jActor;
import ste.ai.viber.actor.Actor;
import ste.ai.viber.actor.StdInStdOutActor;
import ste.ai.viber.cli.command.CliOptions;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.ErrorMessage;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.renderer.StringRenderer;
import ste.ai.viber.tools.InputTool;

import java.util.List;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;

public class MainCommand implements Callable<Integer> {
    private static final Logger LOG = Logger.getLogger(MainCommand.class.getName());

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Mixin
    CliOptions options;

    @Override
    public Integer call() {
        LOG.info("Command-line args: key=" + options.key() + ", endpoint=" + options.endpoint() + ", model=" + options.model() + ", systemPrompt=" + options.systemPrompt() + ", echo=" + options.isEcho() + ", showLog=" + options.isShowLog());

        try {
            validateEndpoint();
        } catch (IllegalArgumentException e) {
            LOG.log(Level.SEVERE, "Invalid endpoint URL", e);
            Conversation conversation = new Conversation();
            Chat chat = new Chat(new PromptMessage("Startup"));
            chat.addMessage(new ErrorMessage("Invalid endpoint: " + e.getMessage(), e));
            new StringRenderer().render(conversation);
            return 1;
        }

        try {
            Actor actor = createActor();
            LogViewerWindow logWindow = null;
            if (options.isShowLog()) {
                new Thread(() -> Application.launch(LogViewerWindow.class)).start();
            }
            Conversation conversation = actor.conversation();
            VibeChatCLI cli = new VibeChatCLI(actor);
            cli.start();
            if (logWindow != null) {
                logWindow.cleanup();
            }
            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
                e.getCause().printStackTrace(System.err);
            } else {
                e.printStackTrace(System.err);
            }
            return 1;
        }
    }

    private void validateEndpoint() {
        try {
            java.net.URI uri = new java.net.URI(options.endpoint());
            String scheme = uri.getScheme();
            if (scheme == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                throw new IllegalArgumentException("Endpoint must use http or https scheme: " + options.endpoint());
            }
        } catch (java.net.URISyntaxException e) {
            throw new IllegalArgumentException("Invalid endpoint URL: " + options.endpoint(), e);
        }
    }

    private Actor createActor() {
        if (options.isEcho()) {
            return new StdInStdOutActor();
        }

        StreamingChatModel chatModel = OpenAiStreamingChatModel.builder()
            .baseUrl(options.endpoint())
            .apiKey(options.key())
            .modelName(options.model())
            .logRequests(true)
            .logResponses(true)
            .build();

        return new LangChain4jActor(
            chatModel,
            List.of(new InputTool()),
            options.systemPrompt(),
            null
        );
    }
}
