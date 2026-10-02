package ste.ai.viber.cli;

import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ste.ai.viber.actor.Actor;
import ste.ai.viber.actor.FXActor;
import ste.ai.viber.actor.LangChain4jActor;
import ste.ai.viber.cli.command.CLIOptions;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.renderer.GUIRenderer;
import ste.ai.viber.tools.FileSystemTools;
import ste.ai.viber.tools.InputTool;


public class MainAppController {

    @FXML
    VBox root;

    @FXML
    GUIRenderer conversation;

    @FXML
    TextArea inputField;

    @FXML
    CheckMenuItem fxActorItem;

    @FXML
    CheckMenuItem langChain4jActorItem;

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
            this.currentActor = new FXActor(conversationModel, this.conversation);
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
                        devModeWindow = new DevModeWindow(fxActor, conversation, owner);
                        devModeWindow.show();
                    }
                };
                owner.showingProperty().addListener(listener[0]);
            }
        }

        this.conversation.render(conversation);

        updateActorMenuSelection();

        fxActorItem.setOnAction(e -> switchToFXActor());
        langChain4jActorItem.setOnAction(e -> switchToLangChain4jActor());

        inputField.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                handleSend();
                e.consume();
            }
        });
    }

    public javafx.scene.Node getRoot() {
        return (javafx.scene.Node) inputField.getParent();
    }

    public String getCurrentActorType() {
        return currentActorType;
    }

    private void updateActorMenuSelection() {
        boolean isFX = "FXActor".equals(currentActorType);
        fxActorItem.setSelected(isFX);
        langChain4jActorItem.setSelected(!isFX);
    }

    private void switchToFXActor() {
        if (switching) return;
        switching = true;
        try {
            FXActor fxActor = new FXActor(conversationModel, conversation);
            this.currentActor = fxActor;
            this.currentActorType = "FXActor";
            updateActorMenuSelection();
            conversation.render(conversationModel);

            if (devModeWindow == null && owner != null && owner.isShowing()) {
                devModeWindow = new DevModeWindow(fxActor, conversationModel, owner);
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

            if (devModeWindow != null) {
                devModeWindow.hide();
                devModeWindow = null;
            }
        } finally {
            switching = false;
        }
    }

    private java.util.List<ste.ai.viber.tools.Tool> defaultTools() {
        try {
            return java.util.List.of(new InputTool(), new FileSystemTools(System.getProperty("user.dir")));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize default tools", e);
        }
    }

    private void handleSend() {
        String text = inputField.getText().trim();
        if (text.isBlank()) {
            return;
        }

        Chat chat = new Chat(new PromptMessage(text));
        conversation.render(chat);
        Thread chatThread = new Thread(() -> currentActor.chat(chat, msg -> conversation.render(msg)));
        chatThread.setDaemon(true);
        chatThread.start();
        inputField.clear();
        inputField.requestFocus();
    }
}
