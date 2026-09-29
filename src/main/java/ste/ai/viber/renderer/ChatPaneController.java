package ste.ai.viber.renderer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;
import ste.ai.viber.model.ChatMessage;

public class ChatPaneController implements Initializable {

    @FXML
    private ScrollPane root;

    @FXML
    private VBox messagesContainer;

    private String lastMessageType = null;
    private MessagePane lastMessagePane = null;

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
        String type = MessagePane.messageTypeFor(message);
        if (!type.equals(lastMessageType) || lastMessagePane == null) {
            MessagePane messagePane = new MessagePane();
            lastMessagePane = messagePane;
            messagePane.setMessage(message);
            addMessage(messagePane.getRoot());
            lastMessageType = type;
        } else {
            lastMessagePane.appendMessage(message);
        }
    }

    public void clear() {
        messagesContainer.getChildren().clear();
        lastMessageType = null;
        lastMessagePane = null;
    }
}
