package ste.ai.viber.cli;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ste.ai.viber.actor.Actor;
import ste.ai.viber.actor.FXActor;
import ste.ai.viber.actor.LangChain4jActor;
import ste.ai.viber.actor.StdInStdOutActor;
import ste.ai.viber.cli.command.CLIOptions;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.renderer.StringRenderer;
import ste.ai.viber.tools.InputTool;
import atlantafx.base.theme.NordLight;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import picocli.CommandLine;

import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;
import javafx.application.HostServices;
import javafx.scene.Parent;

public class ViberApplication extends Application {

    private static final Logger LOG = Logger.getLogger(ViberApplication.class.getName());

    public static HostServices HOST = null;

    @Override
    public void start(Stage primaryStage) {
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());

        System.out.println("parameters: " + getParameters().getRaw());
        String[] args = getParameters().getRaw().toArray(new String[0]);
        CLIOptions options = new CLIOptions();
        CommandLine cmd = new CommandLine(options);
        try {
            cmd.parseArgs(args);
        } catch (CommandLine.ParameterException e) {
            System.err.println(e.getMessage());
            cmd.usage(System.err);
            System.exit(1);
            return;
        }

        HOST = getHostServices();

        primaryStage.setTitle("Viber");

        if (options.isGui()) {
            startGuiMode(primaryStage, options);
            return;
        }

        startCLI(primaryStage, options);
    }

    private void startGuiMode(Stage primaryStage, CLIOptions options) {
        try {
            Actor actor = createActor(options);
            Conversation conversation = actor.conversation();

            MainAppWindow window = new MainAppWindow(conversation, options, actor, primaryStage);
            primaryStage.setScene(newScene(window.getRoot()));
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
                e.getCause().printStackTrace(System.err);
            } else {
                e.printStackTrace(System.err);
            }
            System.exit(1);
        }
    }

    private void startCLI(Stage primaryStage, CLIOptions options) {
        try {
            Actor actor = createActor(options);
            Conversation conversation = actor.conversation();

            if (options.isShowLog()) {
                ste.ai.viber.log.LogViewerWindow.show();
            }

            ViberCLI cli = new ViberCLI(actor, conversation, new StringRenderer(), new InputStreamReader(System.in));
            new Thread(cli::start).start();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
                e.getCause().printStackTrace(System.err);
            } else {
                e.printStackTrace(System.err);
            }
            System.exit(1);
        }
    }

    private Actor createActor(CLIOptions options) {
        if (options.isEcho()) {
            return new StdInStdOutActor();
        }

        if (options.isGui() || options.key() == null) {
            return new FXActor(new Conversation(), null);
        }

        StreamingChatModel chatModel = OpenAiStreamingChatModel.builder()
            .baseUrl(options.endpoint())
            .apiKey(options.key())
            .modelName(options.model())
            .returnThinking(true)
            .logRequests(true)
            .logResponses(true)
            .build();

        return new LangChain4jActor(
            chatModel,
            List.of(new InputTool()),
            options.systemPrompt()
        );
    }

    public static void main(String[] args) {
        Application.launch(ViberApplication.class, args);
    }

    // --------------------------------------------------------- private methods

    private Scene newScene(final Parent parent) {
        final Scene scene = new Scene(parent, 800, 600);

        scene.getStylesheets().addAll(
            "/ste/ai/viber/ui/viber.css"
        );

        return scene;
    }
}
