package ste.ai.viber.renderer;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.ChatMessage;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.util.Safe;

import java.io.IOException;
import java.io.UncheckedIOException;
import static ste.lloop.Loop.on;

/**
 * JavaFX implementation of {@link Renderer}.
 *
 * <p>This renderer manages an internal JavaFX component tree loaded from
 * {@code ConversationPane.fxml}. It never creates or shows a {@link javafx.stage.Stage};
 * the host application is responsible for embedding the root node returned by {@link #getRoot()}
 * into its own scene graph.</p>
 *
 * <p>Rendering semantics:</p>
 * <ul>
 *     <li>{@link #render(Conversation)} clears any previously rendered conversation and
 *         renders all chats as tabs.</li>
 *     <li>{@link #render(Chat)} appends a new tab containing the given chat to the
 *         existing conversation.</li>
 *     <li>{@link #render(ChatMessage)} appends the message to the current (last) chat.
 *         Consecutive messages of the same type are grouped into a single pane;
 *         a different type starts a new pane.</li>
 * </ul>
 */
public class JavaFxRenderer extends ScrollPane implements Renderer {

    private final ConversationPaneController conversationPaneController;

    public JavaFxRenderer() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ConversationPane.fxml"));
        loader.setRoot(this);
        try {
            loader.load();
            this.conversationPaneController = loader.getController();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to initialize JavaFxRenderer", e);
        }
        getStylesheets().add(getClass().getResource("/ste/ai/viber/ui/viber.css").toExternalForm());
        getStyleClass().add("root");
    }

    @Override
    public void render(Conversation conversation) {
        Safe.requireNonNull(conversation, "conversation");
        Platform.runLater(() -> {
            // Remove all previously rendered chats before rendering the new conversation
            conversationPaneController.clear();

            int index = 1;
            for (Chat chat : conversation.chats()) {
                renderChat(chat, index);
                index++;
            }

            if (!conversation.chats().isEmpty()) {
                conversationPaneController.selectLast();
            }
        });
    }

    @Override
    public void render(Chat chat) {
        Safe.requireNonNull(chat, "chat");
        Platform.runLater(() -> {
            // Append a new tab for the chat to the existing conversation
            int index = conversationPaneController.getChatCount() + 1;
            renderChat(chat, index);
            conversationPaneController.selectLast();
        });
    }

    @Override
    public void render(ChatMessage message) {
        Safe.requireNonNull(message, "message");
        Platform.runLater(() -> {
            // Append the message to the current (last) chat
            ChatPaneController chatController = conversationPaneController.getLastChatController();
            if (chatController != null) {
                chatController.appendMessage(message);
            }
        });
    }

    /**
     * Creates the chat UI node and populates it with the chat's messages,
     * then registers it as a tab in the conversation pane.
     */
    private void renderChat(Chat chat, int index) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPane.fxml"));
            Parent chatNode = loader.load();
            ChatPaneController chatController = loader.getController();

            on(chat.messages()).loop((message) -> {
                chatController.appendMessage(message);
            });

            conversationPaneController.addChat(chatNode, "Chat " + index, chatController);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
