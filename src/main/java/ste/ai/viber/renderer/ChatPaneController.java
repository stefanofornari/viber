package ste.ai.viber.renderer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ResourceBundle;
import ste.ai.viber.model.ChatMessage;

public class ChatPaneController implements Initializable {

    @FXML
    private ScrollPane root;

    @FXML
    private VBox messagesContainer;

    private String lastMessageType = null;
    private MessagePaneController lastMessageController = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void addMessage(Node messageNode) {
        messagesContainer.getChildren().add(messageNode);
    }

    /**
     * Appends a message to this chat pane.
     *
     * <p>Consecutive messages of the same type are grouped into a single
     * {@link MessagePaneController}. When the message type changes, a new
     * pane is created.</p>
     *
     * @param message the message to append
     */
    public void appendMessage(ChatMessage message) {
        String type = MessagePaneController.typeNameFor(message);
        if (!type.equals(lastMessageType) || lastMessageController == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MessagePane.fxml"));
            try {
                loader.load();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            lastMessageController = loader.getController();
            lastMessageController.setMessage(message);
            addMessage(loader.getRoot());
            lastMessageType = type;
        } else {
            lastMessageController.appendMessage(message);
        }
    }

    public void clear() {
        messagesContainer.getChildren().clear();
        lastMessageType = null;
        lastMessageController = null;
    }
}
