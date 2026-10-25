package ste.ai.viber.renderer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import ste.llm.chat.model.ChatMessage;
import ste.llm.chat.model.ToolExecutionMessage;
import ste.llm.chat.model.ToolInvocationMessage;


public class ChatPaneController {

    @FXML
    private ScrollPane root;

    @FXML
    private VBox messagesContainer;

    private String lastMessageType = null;
    private Node lastMessagePane = null;
    private final QuestionsPaneManager questionsPanelManager = new QuestionsPaneManager((panel) -> {
        Platform.runLater(() -> {
            addMessage(panel);
            lastMessagePane = panel;
            lastMessageType = "QUESTIONS_PANEL";
        });
    });

    public void addMessage(Node messageNode) {
        if (messageNode instanceof javafx.scene.layout.Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        messagesContainer.getChildren().add(messageNode);
    }

    /**
     * Appends a message to this chat pane. This method is called from the LC4J background thread
     * via the onMessage callback, so all UI updates are wrapped in Platform.runLater.
     *
     * <p>Consecutive messages of the same type are grouped into a single
     * pane. Tool invocation and execution messages are grouped into a
     * {@link ToolMessagePane}: the invocation creates the pane and the
     * execution appends the result below the invocation.</p>
     *
     * <p>For AskTools (questions), a {@link QuestionsPane} is used instead
     * of {@link ToolMessagePane}, allowing users to answer multiple questions
     * in the chat window with per-question Answer buttons.</p>
     *
     * @param message the message to append
     */
    public void appendMessage(ChatMessage message) {
        Platform.runLater(() -> doAppendMessage(message));
    }

    private void doAppendMessage(ChatMessage message) {
        switch (message) {
            case ToolInvocationMessage invocation -> {
                handleToolInvocation(invocation);
            }
            case ToolExecutionMessage execution -> handleToolExecution(execution);
            default -> handleRegularMessage(message);
        }
    }

    private void handleToolInvocation(ToolInvocationMessage invocation) {
        ToolMessagePane pane = new ToolMessagePane();
        pane.invocation(invocation);
        addMessage(pane);
        lastMessagePane = pane;
        lastMessageType = "TOOL_INVOCATION";
    }

    private void handleToolExecution(ToolExecutionMessage execution) {
        if (lastMessagePane instanceof ToolMessagePane toolPane) {
            toolPane.appendResult(execution.content());
        } else if (lastMessagePane instanceof QuestionsPane questionsPanel) {
            // For AskTools, mark the panel as answered
            questionsPanel.getStyleClass().add("questions-panel-answered");
            questionsPanel.setExpanded(false);
        } else {
            ToolMessagePane pane = new ToolMessagePane();
            pane.appendResult(execution.content());
            addMessage(pane);
            lastMessagePane = pane;
            lastMessageType = "TOOL_EXECUTION";
        }
    }

    private void handleRegularMessage(ChatMessage message) {
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

    public void clear() {
        messagesContainer.getChildren().clear();
        lastMessageType = null;
        lastMessagePane = null;
        questionsPanelManager.clear();
    }

    public List<Node> messagesContainerChildren() {
        return new ArrayList<>(messagesContainer.getChildren());
    }
}