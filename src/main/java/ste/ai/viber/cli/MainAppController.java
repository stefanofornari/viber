package ste.ai.viber.cli;

import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ste.ai.viber.actor.Actor;
import ste.ai.viber.actor.FXActor;
import ste.ai.viber.actor.LangChain4jActor;
import ste.ai.viber.actor.ScopeActor;
import ste.ai.viber.cli.command.CLIOptions;
import ste.ai.viber.renderer.GUIRenderer;
import ste.ai.viber.renderer.QuestionsPane;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.Conversation;
import ste.llm.chat.model.PromptMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import java.util.List;
import javafx.scene.Node;
import ste.uzzo.agent.tools.AskTools;
import ste.ai.viber.tools.ViberGUIAskTools;
import ste.llm.tools.FileSystemTools;
import ste.llm.tools.Tool;



public class MainAppController implements Constants {

    @FXML
    VBox root;

    @FXML
    GUIRenderer conversation;

    @FXML
    TextArea prompt;

    @FXML
    VBox inputArea;

    @FXML
    CheckMenuItem fxActorItem;

    @FXML
    CheckMenuItem langChain4jActorItem;

    @FXML
    CheckMenuItem scopeAgentItem;

    @FXML
    Label scopePhaseIndicator;

    private Conversation conversationModel;
    private Actor currentActor;
    private CLIOptions options;
    private String currentActorType = "FXActor";
    private boolean switching = false;
    private DevModeWindow devModeWindow;
    private Stage owner;

    public void initialize(Conversation conversation, CLIOptions options, Stage owner) {
        this.conversationModel = conversation;
        this.options = options;

        if (options == null || options.key() == null || options.key().isBlank()) {
            FXActor fxActor = new FXActor(conversationModel, this.conversation);
            fxActor.askToolConsumer(this::human);
            this.currentActor = fxActor;
            this.currentActorType = "FXActor";
        } else {
            this.currentActor = new LangChain4jActor(
                options.endpoint(),
                options.key(),
                options.model(),
                options.systemPrompt(),
                defaultTools()
            );
            this.currentActorType = "LangChain4jActor";
        }

        this.owner = owner;

        if (currentActor instanceof FXActor fxActor) {
            fxActor.renderer(this.conversation);
            if (owner != null && devModeWindow == null) {
                final javafx.beans.value.ChangeListener<Boolean>[] listener = new javafx.beans.value.ChangeListener[1];
                listener[0] = (obs, oldVal, newVal) -> {
                    if (newVal && devModeWindow == null) {
                        owner.showingProperty().removeListener(listener[0]);
                        devModeWindow = new DevModeWindow(fxActor, conversation, owner, defaultTools());
                        devModeWindow.show();
                    }
                };
                owner.showingProperty().addListener(listener[0]);
            }
        }

        this.conversation.render(conversation);

        updateActorMenuSelection();

        // Initialize scope phase indicator
        scopePhaseIndicator.setText("");

        fxActorItem.setOnAction(e -> switchToFXActor());
        langChain4jActorItem.setOnAction(e -> switchToLangChain4jActor());
        scopeAgentItem.setOnAction(e -> switchToScopeActor());

        prompt.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                handleSend();
                e.consume();
            }
        });
    }

    public javafx.scene.Node getRoot() {
        return (javafx.scene.Node) prompt.getParent();
    }

    public boolean isScopePhaseActive() {
        return "ScopeActor".equals(currentActorType);
    }

    public String getCurrentActorType() {
        return currentActorType;
    }

    public ScopeActor createScopeActor() {
        if (options == null || options.key() == null || options.key().isBlank()) {
            return null;
        }

        StreamingChatModel chatModel = OpenAiStreamingChatModel.builder()
            .baseUrl(options.endpoint())
            .apiKey(options.key())
            .modelName(options.model())
            .returnThinking(true)
            .logRequests(true)
            .logResponses(true)
            .build();

        AskTools askTools = new ViberGUIAskTools(this::human);

        return new ScopeActor(chatModel, askTools);
    }


    public static java.util.List<Tool> defaultTools() {
        try {
            return List.of(new FileSystemTools("."));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize default tools", e);
        }
    }

    // --------------------------------------------------------- private methods

    private void updateActorMenuSelection() {
        boolean isFX = "FXActor".equals(currentActorType);
        boolean isLangChain4j = "LangChain4jActor".equals(currentActorType);
        boolean isScope = "ScopeActor".equals(currentActorType);

        fxActorItem.setSelected(isFX);
        langChain4jActorItem.setSelected(isLangChain4j);
        scopeAgentItem.setSelected(isScope);
    }

    private void switchToFXActor() {
        if (switching) return;
        switching = true;
        try {
            FXActor fxActor = new FXActor(conversationModel, conversation);
            fxActor.askToolConsumer(this::human);
            this.currentActor = fxActor;
            this.currentActorType = "FXActor";
            updateActorMenuSelection();
            conversation.render(conversationModel);
            scopePhaseIndicator.setText("");

            if (devModeWindow == null && owner != null && owner.isShowing()) {
                devModeWindow = new DevModeWindow(fxActor, conversationModel, owner, defaultTools());
                devModeWindow.show();
            }
        } finally {
            switching = false;
        }
    }

    private void switchToLangChain4jActor() {
        if (options == null || options.key() == null || options.key().isBlank()) {
            switchToFXActor();
            return;
        }

        if (switching) return;
        switching = true;
        try {
            LangChain4jActor actor = new LangChain4jActor(
                options.endpoint(),
                options.key(),
                options.model(),
                options.systemPrompt(),
                defaultTools()
            );
            this.currentActor = actor;
            this.currentActorType = "LangChain4jActor";
            updateActorMenuSelection();
            conversation.render(actor.conversation());
            scopePhaseIndicator.setText("");

            if (devModeWindow != null) {
                devModeWindow.hide();
                devModeWindow = null;
            }
        } finally {
            switching = false;
        }
    }

    private void switchToScopeActor() {
        if (switching) return;
        switching = true;
        try {
            if (options == null || options.key() == null || options.key().isBlank()) {
                switchToFXActor();
                return;
            }

            StreamingChatModel chatModel = OpenAiStreamingChatModel.builder()
                .baseUrl(options.endpoint())
                .apiKey(options.key())
                .modelName(options.model())
                .returnThinking(true)
                .logRequests(true)
                .logResponses(true)
                .build();

            AskTools askTools = new ViberGUIAskTools(this::human);

            ScopeActor actor = new ScopeActor(chatModel, askTools);
            this.currentActor = actor;
            this.currentActorType = "ScopeActor";
            updateActorMenuSelection();
            conversation.render(actor.conversation());
            scopePhaseIndicator.setText(LABEL_SCOPE_INDICATOR);
            scopePhaseIndicator.setStyle("-fx-text-fill: orange; -fx-padding: 0 10px;");

            if (devModeWindow != null) {
                devModeWindow.hide();
                devModeWindow = null;
            }
        } finally {
            switching = false;
        }
    }

    private void handleSend() {
        String text = prompt.getText().trim();
        if (text.isBlank()) {
            return;
        }

        // Update scope phase indicator
        if (isScopePhaseActive()) {
            scopePhaseIndicator.setText(LABEL_SCOPE_INDICATOR);
            scopePhaseIndicator.setStyle("-fx-text-fill: orange; -fx-padding: 0 10px;");
        } else {
            scopePhaseIndicator.setText("");
        }

        Chat chat = new Chat(new PromptMessage(text));
        conversation.render(chat);
        Thread chatThread = new Thread(() -> currentActor.chat(chat, msg -> conversation.render(msg)));
        chatThread.setDaemon(true);
        chatThread.start();
        prompt.clear();
        prompt.requestFocus();
    }

    void human(final Node node) {  // TODO refactor into a component in pace of a Node
        // Insert the questions pane just above the prompt TextArea (index 2)
        // If there's already a questions pane at that position, replace it
        if (root.getChildren().size() > 2 && root.getChildren().get(2) instanceof QuestionsPane) {
            root.getChildren().set(2, node);
        } else {
            root.getChildren().add(2, node);
        }
    }
}
