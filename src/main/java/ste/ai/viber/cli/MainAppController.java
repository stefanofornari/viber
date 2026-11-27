package ste.ai.viber.cli;

import javafx.beans.binding.Bindings;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ste.ai.viber.actor.Actor;
import ste.ai.viber.actor.ChatModels;
import ste.ai.viber.actor.CodingActor;
import ste.ai.viber.actor.FXActor;
import ste.ai.viber.actor.LangChain4jActor;
import ste.ai.viber.actor.ScopeActor;
import ste.ai.viber.actor.StoryActor;
import ste.ai.viber.cli.command.CLIOptions;
import ste.ai.viber.renderer.GUIRenderer;
import ste.ai.viber.renderer.QuestionsPane;
import ste.fx.control.radialmenu.MenuItem;
import ste.fx.control.radialmenu.RadialMenu;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.Conversation;
import ste.llm.chat.model.PromptMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import java.util.function.Supplier;
import java.nio.file.Path;
import java.util.List;
import ste.ai.viber.tools.ViberGUIAskTools;
import ste.llm.tools.FileSystemTools;
import ste.llm.tools.Tool;
import java.util.logging.Level;
import java.util.logging.Logger;



public class MainAppController implements Constants {

    private static final Logger LOG = Logger.getLogger(MainAppController.class.getName());
    private static final double ASK_TOOL_PROMPT_HEIGHT = 75;
    private static final double RADIAL_SIZE = 125;
    private static final double HUB_SIZE = 15;

    @FXML
    VBox root;

    @FXML
    GUIRenderer conversation;

    @FXML
    TextArea prompt;

    @FXML
    HBox promptRow;

    @FXML
    StackPane radialSlot;

    @FXML
    VBox inputArea;

    @FXML
    HBox statusLine;

    @FXML
    Label projectName;

    @FXML
    Label workingDirectory;

    @FXML
    CheckMenuItem fxActorItem;

    @FXML
    CheckMenuItem langChain4jActorItem;

    @FXML
    CheckMenuItem scopeAgentItem;

    @FXML
    CheckMenuItem storyAgentItem;

    @FXML
    CheckMenuItem codingActorItem;

    private Conversation conversationModel;
    private final ConversationManager conversationManager = new ConversationManager();
    private Actor currentActor;
    private CLIOptions options;
    private ActorType currentActorType = ActorType.FX;
    private boolean switching = false;
    private DevModeWindow devModeWindow;
    private Stage owner;
    private RadialMenu radialMenu;
    private Label radialHub;
    private RadialMode radialMode;
    private String radialError;

    public void initialize(Conversation conversation, CLIOptions options, Stage owner) {
        this.conversationModel = conversation;
        this.options = options;
        inputArea.getChildren().addListener((ListChangeListener<Node>) change -> updateAskToolLayout());

        if (!hasApiKey()) {
            FXActor fxActor = new FXActor(conversationModel, this.conversation);
            fxActor.askToolConsumer(this::human);
            this.currentActor = fxActor;
            this.currentActorType = ActorType.FX;
        } else {
            this.currentActor = createLangChain4jActor();
            this.currentActorType = ActorType.LANGCHAIN4J;
        }

        this.owner = owner;

        if (currentActor instanceof FXActor fxActor) {
            fxActor.renderer(this.conversation);
            if (owner != null && devModeWindow == null) {
                final javafx.beans.value.ChangeListener<Boolean>[] listener = new javafx.beans.value.ChangeListener[1];
                listener[0] = (obs, oldVal, newVal) -> {
                    if (newVal && devModeWindow == null) {
                        owner.showingProperty().removeListener(listener[0]);
                        devModeWindow = new DevModeWindow(fxActor, conversation, owner, defaultTools(), conversationManager);
                        devModeWindow.show();
                    }
                };
                owner.showingProperty().addListener(listener[0]);
            }
        }

        this.conversation.archiveHandler(this::archive);
        this.conversation.forgetHandler(this::persistenceNotImplemented);
        this.conversation.render(conversation);

        updateActorMenuSelection();

        initializeRadialSelector();

        statusLine(new ProjectInfo(Path.of(System.getProperty("user.dir"))));

        fxActorItem.setOnAction(e -> {
            switchToFXActor();
            onMenuActorSwitch();
        });
        langChain4jActorItem.setOnAction(e -> {
            switchToLangChain4jActor();
            onMenuActorSwitch();
        });
        scopeAgentItem.setOnAction(e -> {
            switchToScopeActor();
            onMenuActorSwitch();
        });
        storyAgentItem.setOnAction(e -> {
            switchToStoryActor();
            onMenuActorSwitch();
        });
        codingActorItem.setOnAction(e -> {
            switchToCodingActor();
            onMenuActorSwitch();
        });

        prompt.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                handleSend();
                e.consume();
            }
        });
    }

    private boolean archive(Chat chat) {
        boolean archived = persistenceNotImplemented(chat);
        if (conversationManager.isCurrent(chat)) {
            // the next prompt starts a new chat, in a new tab
            conversationManager.reset();
            conversation.newChat();
        }
        return archived;
    }

    private boolean persistenceNotImplemented(Chat chat) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Persistence unavailable");
        alert.setHeaderText(null);
        alert.setContentText("persistence not yet implemented");
        alert.show();
        return false;
    }

    public javafx.scene.Node getRoot() {
        return root;
    }

    /**
     * Shows the given project in the status line: its name and the full
     * absolute path of its working directory (the path is also available as a
     * tooltip, as it may be truncated in the middle to fit the window).
     *
     * @param project the project to show
     */
    void statusLine(final ProjectInfo project) {
        final String path = project.workingDirectory().toString();

        projectName.setText("Project: " + project.name());
        workingDirectory.setText("Project folder: " + path);
        workingDirectory.setTooltip(new Tooltip(path));
    }

    Label radialHub() {
        return radialHub;
    }

    public boolean isScopePhaseActive() {
        return currentActorType == ActorType.SCOPE;
    }

    public String getCurrentActorType() {
        return currentActorType.label();
    }

    public ScopeActor createScopeActor() {
        if (!hasApiKey()) {
            return null;
        }
        return new ScopeActor(chatModel(), new ViberGUIAskTools(this::human), fileSystemTools());
    }

    private StoryActor createStoryActor() {
        return new StoryActor(chatModel(), new ViberGUIAskTools(this::human), fileSystemTools());
    }

    /**
     * Creates a coding actor using the configured LLM connection and project tools.
     *
     * @return the configured coding actor
     * @throws IllegalStateException if no API key is configured
     */
    public CodingActor createCodingActor() {
        if (!hasApiKey()) {
            throw new IllegalStateException("An API key is required to initialize CodingActor");
        }
        return new CodingActor(chatModel(), fileSystemTools());
    }

    private LangChain4jActor createLangChain4jActor() {
        return new LangChain4jActor(chatModel(), defaultTools(), options.systemPrompt());
    }

    private boolean hasApiKey() {
        return options != null && options.key() != null && !options.key().isBlank();
    }

    private StreamingChatModel chatModel() {
        return ChatModels.openAi(options.endpoint(), options.key(), options.model());
    }

    private static FileSystemTools fileSystemTools() {
        try {
            return new FileSystemTools(".");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize FileSystemTools", e);
        }
    }

    public static java.util.List<Tool> defaultTools() {
        return List.of(fileSystemTools());
    }

    // --------------------------------------------------------- private methods

    private void initializeRadialSelector() {
        radialHub = new Label(LABEL_HUB_NEUTRAL);
        radialHub.setId("radialHub");
        radialHub.getStyleClass().add("radial-hub");
        radialHub.setMouseTransparent(true);

        radialSlot.setPrefSize(RADIAL_SIZE, RADIAL_SIZE);
        radialSlot.setMinSize(RADIAL_SIZE, RADIAL_SIZE);
        radialSlot.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        radialMenu = createRadialMenu();
        radialSlot.getChildren().setAll(radialMenu, radialHub);
        preselectExploreMode();
        updateHub();
    }

    private void preselectExploreMode() {
        radialMenu.items().stream()
            .filter(item -> item.actionPayload() == RadialMode.EXPLORE)
            .findFirst()
            .ifPresent(radialMenu::select);
        radialMode = RadialMode.EXPLORE;
    }

    private RadialMenu createRadialMenu() {
        final List<MenuItem<RadialMode>> items = List.of(
            new MenuItem<>("Scope", RadialMode.SCOPE),
            new MenuItem<>("Story", RadialMode.STORY),
            new MenuItem<>("Code", RadialMode.CODE),
            new MenuItem<>("Explore", RadialMode.EXPLORE)
        );
        RadialMenu menu = new RadialMenu(items,
            selected -> handleRadialSelection((RadialMode) selected.actionPayload()));
        menu.setId("radialMenu");
        menu.innerRadiusRatioProperty().bind(Bindings.createDoubleBinding(
            () -> menu.diameter() <= HUB_SIZE ? 0.0 : HUB_SIZE / menu.diameter(),
            menu.diameterProperty()));
        menu.getStylesheets().add(
            MainAppController.class.getResource("/ste/ai/viber/ui/viber.css").toExternalForm());
        menu.diameterProperty().bind(radialSlot.widthProperty());
        return menu;
    }

    private void handleRadialSelection(RadialMode mode) {
        radialMode = mode;
        // a different actor owns a different conversation: next prompt starts a new chat
        conversationManager.reset();
        switch (mode) {
            case SCOPE -> switchToScopeActor();
            case STORY -> switchToStoryActor();
            case CODE -> switchToCodingActor();
            case EXPLORE -> switchToLangChain4jActor();
        }

        if (matchesActiveActor(mode)) {
            updateHub();
        } else {
            javafx.application.Platform.runLater(() -> {
                resetRadialHighlight();
                updateHub();
            });
        }
    }

    private void onMenuActorSwitch() {
        // a different actor owns a different conversation: next prompt starts a new chat
        conversationManager.reset();
        resetRadialHighlight();
        updateHub();
    }

    private void resetRadialHighlight() {
        radialMode = null;
        if (radialMenu != null) {
            radialMenu.diameterProperty().unbind();
        }
        radialMenu = createRadialMenu();
        radialSlot.getChildren().setAll(radialMenu, radialHub);
    }

    private void showRadialError(String message) {
        radialError = message;
        updateHub();
    }

    private void updateHub() {
        if (radialError != null) {
            radialHub.setText("!");
            if (!radialHub.getStyleClass().contains("radial-hub-error")) {
                radialHub.getStyleClass().add("radial-hub-error");
            }
            Tooltip tooltip = radialHub.getTooltip();
            if (tooltip == null) {
                radialHub.setTooltip(new Tooltip(radialError));
            } else {
                tooltip.setText(radialError);
            }
            return;
        }
        radialHub.getStyleClass().remove("radial-hub-error");
        radialHub.setTooltip(null);
    }

    private boolean matchesActiveActor(RadialMode mode) {
        return mode.actorType == currentActorType;
    }

    private void updateActorMenuSelection() {
        boolean isFX = currentActorType == ActorType.FX;
        boolean isLangChain4j = currentActorType == ActorType.LANGCHAIN4J;
        boolean isScope = currentActorType == ActorType.SCOPE;
        boolean isStory = currentActorType == ActorType.STORY;
        boolean isCoding = currentActorType == ActorType.CODING;

        fxActorItem.setSelected(isFX);
        langChain4jActorItem.setSelected(isLangChain4j);
        scopeAgentItem.setSelected(isScope);
        storyAgentItem.setSelected(isStory);
        codingActorItem.setSelected(isCoding);
    }

    private void switchToFXActor() {
        if (switching) return;
        switching = true;
        try {
            FXActor fxActor = new FXActor(conversationModel, conversation);
            fxActor.askToolConsumer(this::human);
            this.currentActor = fxActor;
            this.currentActorType = ActorType.FX;
            updateActorMenuSelection();
            conversation.render(conversationModel);
            radialError = null;

            if (devModeWindow == null && owner != null && owner.isShowing()) {
                devModeWindow = new DevModeWindow(fxActor, conversationModel, owner, defaultTools(), conversationManager);
                devModeWindow.show();
            }
        } finally {
            switching = false;
        }
    }

    /** Switches to an LLM-backed actor, falling back to the manual actor when no key is configured. */
    private void switchTo(ActorType type, Supplier<? extends Actor> factory) {
        if (!hasApiKey()) {
            switchToFXActor();
            return;
        }
        if (switching) return;
        switching = true;
        try {
            final Actor actor = factory.get();
            this.currentActor = actor;
            this.currentActorType = type;
            updateActorMenuSelection();
            conversation.render(actor.conversation());
            radialError = null;
            closeDevModeWindow();
        } finally {
            switching = false;
        }
    }

    private void switchToLangChain4jActor() {
        switchTo(ActorType.LANGCHAIN4J, this::createLangChain4jActor);
    }

    private void switchToScopeActor() {
        switchTo(ActorType.SCOPE, this::createScopeActor);
    }

    private void switchToStoryActor() {
        switchTo(ActorType.STORY, this::createStoryActor);
    }

    private void switchToCodingActor() {
        if (switching) return;
        switching = true;
        try {
            CodingActor actor;
            try {
                actor = createCodingActor();
                conversation.render(actor.conversation());
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, "Failed to initialize CodingActor", e);
                showRadialError("Coding phase unavailable: " + e.getMessage());
                updateActorMenuSelection();
                return;
            }

            this.currentActor = actor;
            this.currentActorType = ActorType.CODING;
            radialError = null;
            updateActorMenuSelection();
            closeDevModeWindow();
        } finally {
            switching = false;
        }
    }

    private void closeDevModeWindow() {
        if (devModeWindow != null) {
            devModeWindow.hide();
            devModeWindow = null;
        }
    }

    private void handleSend() {
        String text = prompt.getText().trim();
        if (text.isBlank()) {
            return;
        }

        // Keep working on the same chat until it is archived
        final ConversationManager.Turn turn = conversationManager.prompt(text);
        final Chat chat = turn.chat();
        if (turn.newChat()) {
            conversation.render(chat);
        } else {
            conversation.render(turn.prompt());
        }
        conversation.communicating(chat, true);
        final Thread chatThread;
        if (currentActor instanceof FXActor fxActor) {
            // the manual actor answers later, from the dev mode window
            fxActor.communicationEndHandler(c -> conversation.communicating(c, false));
            chatThread = new Thread(() -> fxActor.chat(chat, msg -> conversation.render(msg)));
        } else {
            chatThread = new Thread(() -> {
                try {
                    currentActor.chat(chat, msg -> conversation.render(msg));
                } finally {
                    conversation.communicating(chat, false);
                }
            });
        }
        chatThread.setDaemon(true);
        chatThread.start();
        prompt.clear();
        prompt.requestFocus();
    }

    public boolean isStoryPhaseActive() {
        return currentActorType == ActorType.STORY;
    }

    /**
     * Returns whether CodingActor is the active actor.
     *
     * @return true when the coding phase is active
     */
    public boolean isCodingPhaseActive() {
        return currentActorType == ActorType.CODING;
    }

    void human(final Node node) {  // TODO refactor into a component in pace of a Node
        // Insert the tool pane above the prompt; replace any pane already shown.
        if (inputArea.getChildren().size() > 1 && inputArea.getChildren().get(0) instanceof QuestionsPane) {
            inputArea.getChildren().set(0, node);
        } else {
            inputArea.getChildren().add(0, node);
        }
    }

    private void updateAskToolLayout() {
        QuestionsPane questionsPane = inputArea.getChildren().stream()
            .filter(QuestionsPane.class::isInstance)
            .map(QuestionsPane.class::cast)
            .findFirst()
            .orElse(null);

        if (questionsPane == null) {
            VBox.setVgrow(promptRow, Priority.ALWAYS);
            prompt.setMinHeight(Region.USE_COMPUTED_SIZE);
            prompt.setPrefHeight(Region.USE_COMPUTED_SIZE);
            prompt.setMaxHeight(Double.MAX_VALUE);
            return;
        }

        VBox.setVgrow(promptRow, Priority.NEVER);
        prompt.setMinHeight(ASK_TOOL_PROMPT_HEIGHT);
        prompt.setPrefHeight(ASK_TOOL_PROMPT_HEIGHT);
        prompt.setMaxHeight(ASK_TOOL_PROMPT_HEIGHT);
        VBox.setVgrow(questionsPane, Priority.ALWAYS);
    }

    private enum RadialMode {
        SCOPE("Scope", ActorType.SCOPE),
        STORY("Story", ActorType.STORY),
        CODE("Code", ActorType.CODING),
        EXPLORE("Explore", ActorType.LANGCHAIN4J);

        private final String label;
        private final ActorType actorType;

        RadialMode(String label, ActorType actorType) {
            this.label = label;
            this.actorType = actorType;
        }
    }
}
