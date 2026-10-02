package ste.ai.viber.renderer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;
import ste.ai.viber.model.ChatMessage;
import ste.ai.viber.model.ToolExecutionMessage;
import ste.ai.viber.model.ToolInvocationMessage;

public class ChatPaneController implements Initializable {

    @FXML
    private ScrollPane root;

    @FXML
    private VBox messagesContainer;

    private String lastMessageType = null;
    private Node lastMessagePane = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void addMessage(Node messageNode) {
        if (messageNode instanceof javafx.scene.layout.Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        messagesContainer.getChildren().add(messageNode);
    }

    /**
     * Appends a message to this chat pane.
     *
     * <p>Consecutive messages of the same type are grouped into a single
     * pane. Tool invocation and execution messages are grouped into a
     * {@link ToolMessagePane}: the invocation creates the pane and the
     * execution appends the result below the invocation.</p>
     *
     * @param message the message to append
     */
    public void appendMessage(ChatMessage message) {
        if (message instanceof ToolInvocationMessage invocation) {
            ToolMessagePane pane = new ToolMessagePane();
            pane.invocation(invocation.content());
            addMessage(pane);
            lastMessagePane = pane;
            lastMessageType = "TOOL_INVOCATION";
        } else if (message instanceof ToolExecutionMessage execution) {
            if (lastMessagePane instanceof ToolMessagePane toolPane) {
                toolPane.appendResult(execution.content());
            } else {
                ToolMessagePane pane = new ToolMessagePane();
                pane.appendResult(execution.content());
                addMessage(pane);
                lastMessagePane = pane;
                lastMessageType = "TOOL_EXECUTION";
            }
        } else {
            String type = MessagePane.messageTypeFor(message);
            if (!type.equals(lastMessageType) || lastMessagePane == null) {
                MessagePane messagePane = new MessagePane();
                lastMessagePane = messagePane;
                messagePane.message(message);
                addMessage(messagePane);
                lastMessageType = type;
            } else {
                ((MessagePane) lastMessagePane).appendMessage(message);
            }
        }
    }

    public void clear() {
        messagesContainer.getChildren().clear();
        lastMessageType = null;
        lastMessagePane = null;
    }
}
