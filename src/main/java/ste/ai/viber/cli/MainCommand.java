package ste.ai.viber.cli;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import picocli.CommandLine;
import ste.ai.viber.LangChain4jActor;
import ste.ai.viber.actor.Actor;
import ste.ai.viber.actor.StdInStdOutActor;
import ste.ai.viber.cli.command.CliOptions;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.tools.InputTool;

import java.util.List;

import java.util.concurrent.Callable;

public class MainCommand implements Callable<Integer> {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Mixin
    CliOptions options;

    @Override
    public Integer call() {
        try {
            Actor actor = createActor();
            Conversation conversation = actor.conversation();
            VibeChatCLI cli = new VibeChatCLI(actor);
            cli.start();
            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private Actor createActor() {
        if (options.isEcho()) {
            return new StdInStdOutActor();
        }

        ChatModel chatModel = OpenAiChatModel.builder()
            .baseUrl(options.endpoint())
            .apiKey(options.key())
            .modelName(options.model())
            .build();

        return new LangChain4jActor(
            chatModel,
            List.of(new InputTool()),
            options.systemPrompt(),
            null
        );
    }
}
