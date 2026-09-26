package ste.ai.viber.cli;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import ste.ai.viber.actor.Actor;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.renderer.JavaFxRenderer;

public class MainAppController {

    @FXML
    StackPane conversationContainer;

    @FXML
    TextArea inputField;

    private Actor actor;
    private JavaFxRenderer renderer;

    public void initialize(Actor actor, Conversation conversation, JavaFxRenderer renderer) {
        this.actor = actor;
        this.renderer = renderer;
        conversationContainer.getChildren().add(renderer.getRoot());
        renderer.render(conversation);

        inputField.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                handleSend();
                e.consume();
            }
        });
    }

    private void handleSend() {
        String text = inputField.getText().trim();
        if (text.isBlank()) {
            return;
        }

        Chat chat = new Chat(new PromptMessage(text));
        actor.chat(chat, msg -> renderer.render(msg));
        inputField.clear();
        inputField.requestFocus();
    }
}
