package ste.ai.viber.cli;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import ste.ai.viber.actor.FXActor;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.renderer.JavaFxRenderer;

public class DevModeController {

    @FXML
    StackPane conversationContainer;

    @FXML
    TextArea inputText;

    @FXML
    Button addChatButton;

    @FXML
    Button addReplyButton;

    @FXML
    Button addThoughtButton;

    @FXML
    Button addToolExecutionButton;

    @FXML
    Button addToolReplyButton;

    @FXML
    Button addErrorButton;

    private FXActor fxActor;
    private JavaFxRenderer renderer;

    public void initialize(FXActor fxActor, Conversation conversation, JavaFxRenderer renderer) {
        this.fxActor = fxActor;
        this.renderer = renderer;
        conversationContainer.getChildren().add(renderer);
        renderer.render(conversation);

        addChatButton.setOnAction(e -> handleAddChat());
        addReplyButton.setOnAction(e -> handleAddReply());
        addThoughtButton.setOnAction(e -> handleAddThought());
        addToolExecutionButton.setOnAction(e -> handleAddToolExecution());
        addToolReplyButton.setOnAction(e -> handleAddToolReply());
        addErrorButton.setOnAction(e -> handleAddError());

        inputText.textProperty().addListener((obs, old, text) -> updateButtonStates());
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean enabled = !inputText.getText().isBlank();
        addChatButton.setDisable(!enabled);
        addReplyButton.setDisable(!enabled);
        addThoughtButton.setDisable(!enabled);
        addToolExecutionButton.setDisable(!enabled);
        addToolReplyButton.setDisable(!enabled);
        addErrorButton.setDisable(!enabled);
    }

    private void handleAddChat() {
        String text = inputText.getText().trim();
        if (text.isBlank()) {
            return;
        }
        fxActor.chat(new Chat(new PromptMessage(text)), msg -> {});
        inputText.clear();
    }

    private void handleAddReply() {
        String text = inputText.getText().trim();
        if (text.isBlank()) {
            return;
        }
        fxActor.addReply(text);
        inputText.clear();
    }

    private void handleAddThought() {
        String text = inputText.getText().trim();
        if (text.isBlank()) {
            return;
        }
        fxActor.addThought(text);
        inputText.clear();
    }

    private void handleAddToolExecution() {
        String text = inputText.getText().trim();
        if (text.isBlank()) {
            return;
        }
        fxActor.addToolExecution(text);
        inputText.clear();
    }

    private void handleAddToolReply() {
        String text = inputText.getText().trim();
        if (text.isBlank()) {
            return;
        }
        fxActor.addToolReply(text);
        inputText.clear();
    }

    private void handleAddError() {
        String text = inputText.getText().trim();
        if (text.isBlank()) {
            return;
        }
        fxActor.addError(text);
        inputText.clear();
    }
}
