package ste.ai.viber.renderer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
    private final Deque<PendingToolResult> pendingToolResults = new ArrayDeque<>();

    @FXML
    private void initialize() {
        root.addEventFilter(ScrollEvent.SCROLL, this::handleScroll);
    }

    private void handleScroll(ScrollEvent event) {
        Node target = event.getTarget() instanceof Node node ? node : root;
        if (event.getDeltaY() != 0) {
            scrollAxis(target, event.getDeltaY(), false);
        }
        if (event.getDeltaX() != 0) {
            scrollAxis(target, event.getDeltaX(), true);
        }
        event.consume();
    }

    private void scrollAxis(Node target, double delta, boolean horizontal) {
        Node current = target;
        while (current != null) {
            if (current instanceof ScrollPane scrollPane && scrollBy(scrollPane, delta, horizontal)) {
                return;
            }
            current = current.getParent();
        }
    }

    private boolean scrollBy(ScrollPane scrollPane, double delta, boolean horizontal) {
        Node content = scrollPane.getContent();
        if (content == null) {
            return false;
        }

        double contentSize = horizontal
            ? content.getLayoutBounds().getWidth()
            : content.getLayoutBounds().getHeight();
        double viewportSize = horizontal
            ? scrollPane.getViewportBounds().getWidth()
            : scrollPane.getViewportBounds().getHeight();
        double overflow = contentSize - viewportSize;
        if (overflow <= 0) {
            return false;
        }

        double min = horizontal ? scrollPane.getHmin() : scrollPane.getVmin();
        double max = horizontal ? scrollPane.getHmax() : scrollPane.getVmax();
        double current = horizontal ? scrollPane.getHvalue() : scrollPane.getVvalue();
        double direction = horizontal ? 1 : -1;
        double next = Math.max(min, Math.min(max, current + direction * delta / overflow * (max - min)));
        if (next == current) {
            return false;
        }

        if (horizontal) {
            scrollPane.setHvalue(next);
        } else {
            scrollPane.setVvalue(next);
        }
        return true;
    }

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
     * pane. Regular tool invocations and their results are grouped into a
     * {@link ToolMessagePane}.</p>
     *
     * <p>AskTools questions are displayed separately in a {@link QuestionsPane}
     * above the prompt. Their execution results are displayed as tool response
     * messages in the chat, without a tool invocation pane.</p>
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
        if (isAskTool(invocation.invocation.name)) {
            String question = invocation.invocation.arguments.optString("question", "");
            pendingToolResults.addLast(new PendingToolResult(question, null));
            return;
        }

        ToolMessagePane pane = new ToolMessagePane();
        pane.invocation(invocation);
        addMessage(pane);
        lastMessagePane = pane;
        lastMessageType = "TOOL_INVOCATION";
        pendingToolResults.addLast(new PendingToolResult(null, pane));
    }

    private void handleToolExecution(ToolExecutionMessage execution) {
        PendingToolResult pending = pendingToolResults.pollFirst();
        if (pending != null && pending.isAskTool()) {
            handleAskToolResponse(pending.question(), execution);
            return;
        }
        if (pending != null && pending.pane() != null) {
            pending.pane().appendResult(execution.content());
            return;
        }
        if ("TOOL_EXECUTION_RESPONSE".equals(lastMessageType)) {
            handleRegularMessage(execution);
            return;
        }
        if (lastMessagePane instanceof ToolMessagePane toolPane) {
            toolPane.appendResult(execution.content());
        } else {
            ToolMessagePane pane = new ToolMessagePane();
            pane.appendResult(execution.content());
            addMessage(pane);
            lastMessagePane = pane;
            lastMessageType = "TOOL_EXECUTION";
        }
    }

    private void handleAskToolResponse(String question, ToolExecutionMessage response) {
        if (lastMessagePane instanceof MessagePane pane
            && "TOOL_EXECUTION_RESPONSE".equals(lastMessageType)) {
            pane.appendAskToolResponse(question, response);
            return;
        }

        MessagePane pane = new MessagePane();
        pane.askToolResponse(question, response);
        addMessage(pane);
        lastMessagePane = pane;
        lastMessageType = "TOOL_EXECUTION_RESPONSE";
    }

    private boolean isAskTool(String toolName) {
        return "AskQuestion".equals(toolName)
            || "AskSingleChoice".equals(toolName)
            || "AskMultipleChoice".equals(toolName)
            || "AskConfirmation".equals(toolName);
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
        pendingToolResults.clear();
    }

    public List<Node> messagesContainerChildren() {
        return new ArrayList<>(messagesContainer.getChildren());
    }

    private record PendingToolResult(String question, ToolMessagePane pane) {
        boolean isAskTool() {
            return question != null;
        }
    }
}