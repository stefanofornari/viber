package ste.ai.viber.renderer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TabPane;

import ste.lazyjava.safe.Safe;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.ChatMessage;
import ste.llm.chat.model.Conversation;

import static ste.lloop.Loop.on;

/**
 * GUI implementation of {@link Renderer}.
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
public class GUIRenderer extends TabPane implements Renderer, Safe {

    private final ConversationPaneController conversationPaneController;

    public GUIRenderer() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ConversationPane.fxml"));
        loader.setRoot(this);
        try {
            loader.load();
            this.conversationPaneController = loader.getController();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to initialize GUIRenderer", e);
        }
        getStylesheets().add(getClass().getResource("/ste/ai/viber/ui/viber.css").toExternalForm());
        getStyleClass().add("root");
    }

    /**
     * Configures the archive hook used for chats rendered after this call.
     * The handler should return {@code true} only after retaining the chat, which allows its tab
     * to close; returning {@code false} leaves the tab open.
     *
     * @param archiveHandler handler that retains a chat and reports whether it succeeded
     */
    public void archiveHandler(Predicate<Chat> archiveHandler) {
        requireNonNull(archiveHandler, "archiveHandler");
        conversationPaneController.archiveHandler(archiveHandler);
    }

    /**
     * Configures the forget hook used for chats rendered after this call.
     * The handler should return {@code true} only after discarding the chat, which allows its tab
     * to close; returning {@code false} leaves the tab open.
     *
     * @param forgetHandler handler that discards a chat and reports whether it succeeded
     */
    public void forgetHandler(Predicate<Chat> forgetHandler) {
        requireNonNull(forgetHandler, "forgetHandler");
        conversationPaneController.forgetHandler(forgetHandler);
    }

    /**
     * Opens a new empty chat tab (unless one is already waiting) and selects it, so
     * that the next chat goes there.
     */
    public void newChat() {
        Platform.runLater(() -> {
            if (!conversationPaneController.hasEmptyChat()) {
                renderEmptyChat();
            }
            conversationPaneController.selectLast();
        });
    }

    @Override
    public void render(Conversation conversation) {
        requireNonNull(conversation, "conversation");
        Platform.runLater(() -> {
            // Remove all previously rendered chats before rendering the new conversation
            conversationPaneController.clear();

            int index = 1;
            for (Chat chat : conversation.chats()) {
                renderChat(chat, index);
                index++;
            }

            if (conversation.chats().isEmpty()) {
                renderEmptyChat();
            }

            if (!conversation.chats().isEmpty()) {
                conversationPaneController.selectLast();
            }
        });
    }

    @Override
    public void render(Chat chat) {
        requireNonNull(chat, "chat");
        Platform.runLater(() -> {
            // Append a new tab for the chat to the existing conversation
            int index = conversationPaneController.getChatCount() + 1;
            renderChat(chat, index);
            conversationPaneController.selectLast();
        });
    }

    @Override
    public void render(ChatMessage message) {
        requireNonNull(message, "message");
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
            ChatPaneView chatPane = loadChatPane();

            on(chat.messages()).loop((message) -> {
                chatPane.controller().appendMessage(message);
            });

            conversationPaneController.addChat(chatPane.node(), "Chat " + index, chat, chatPane.controller());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void renderEmptyChat() {
        try {
            ChatPaneView chatPane = loadChatPane();
            conversationPaneController.addEmptyChat(chatPane.node(), "Chat 1", chatPane.controller());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ChatPaneView loadChatPane() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPane.fxml"));
        Parent chatNode = loader.load();
        return new ChatPaneView(chatNode, loader.getController());
    }

    private record ChatPaneView(Parent node, ChatPaneController controller) {
    }
}
