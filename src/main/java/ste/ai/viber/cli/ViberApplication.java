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
import ste.ai.viber.renderer.JavaFxRenderer;
import ste.ai.viber.renderer.StringRenderer;
import ste.ai.viber.tools.InputTool;
import atlantafx.base.theme.NordLight;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import picocli.CommandLine;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.logging.Logger;
import javafx.application.HostServices;

public class ViberApplication extends Application {

    private static final Logger LOG = Logger.getLogger(ViberApplication.class.getName());

    public static HostServices HOST = null;

    @Override
    public void start(Stage primaryStage) {
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());

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

        if (options.isDev()) {
            startDevMode(primaryStage);
            return;
        }

        if (options.isFxRender()) {
            startCLIWithFx(primaryStage, options);
            return;
        }

        if (options.isGui() || options.key() == null) {
            startStandaloneGui(primaryStage);
            return;
        }

        startCLI(primaryStage, options);
    }

    private void startDevMode(Stage primaryStage) {
        JavaFxRenderer fxRenderer = new JavaFxRenderer();
        primaryStage.setScene(new Scene(fxRenderer.getRoot(), 800, 600));
        primaryStage.show();

        Conversation conversation = new Conversation();
        Actor actor = new FXActor(conversation, fxRenderer);
        DevModeWindow devModeWindow = new DevModeWindow((FXActor) actor, conversation);
        devModeWindow.show();

        ViberCLI cli = new ViberCLI(actor, conversation, fxRenderer, new InputStreamReader(System.in));
        new Thread(cli::start).start();
    }

    private void startCLIWithFx(Stage primaryStage, CLIOptions options) {
        JavaFxRenderer fxRenderer = new JavaFxRenderer();
        primaryStage.setScene(new Scene(fxRenderer.getRoot(), 800, 600));
        primaryStage.show();

        try {
            Actor actor = createActor(options);
            Conversation conversation = actor.conversation();
            fxRenderer.render(conversation);

            if (options.isShowLog()) {
                ste.ai.viber.log.LogViewerWindow.show();
            }

            Reader input = new InputStreamReader(System.in);
            ViberCLI cli = new ViberCLI(actor, conversation, fxRenderer, input);
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

    private void startStandaloneGui(Stage primaryStage) {
        Conversation conversation = new Conversation();
        JavaFxRenderer renderer = new JavaFxRenderer();
        Actor actor = new FXActor(conversation, renderer);

        MainAppWindow window = new MainAppWindow(actor, conversation, renderer);
        primaryStage.setScene(new Scene(window.getRoot(), 800, 600));
        primaryStage.show();
    }

    private void startCLI(Stage primaryStage, CLIOptions options) {
        primaryStage.setWidth(0);
        primaryStage.setHeight(0);
        primaryStage.setX(-1000);
        primaryStage.setY(-1000);
        primaryStage.show();
        primaryStage.hide();

        try {
            Actor actor = createActor(options);
            Conversation conversation = actor.conversation();

            if (options.isShowLog()) {
                ste.ai.viber.log.LogViewerWindow.show();
            }

            Reader input = new InputStreamReader(System.in);
            ViberCLI cli = new ViberCLI(actor, conversation, new StringRenderer(), input);
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

        if (options.key() == null) {
            throw new IllegalArgumentException("--key is required for CLI mode with LangChain4jActor");
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
}
