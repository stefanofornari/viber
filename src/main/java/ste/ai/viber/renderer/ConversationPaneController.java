package ste.ai.viber.renderer;

import atlantafx.base.controls.RingProgressIndicator;
import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.ChatMessage;
import ste.llm.chat.model.ToolInvocationMessage;

import java.net.URL;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.logging.Logger;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;

public class ConversationPaneController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ConversationPaneController.class.getName());
    private static final ButtonType FORGET_BUTTON = new ButtonType("Forget", ButtonBar.ButtonData.OK_DONE);

    private static final String CHAT_KEY = "chat";
    private static final String INDICATOR_KEY = "activityIndicator";
    private static final double INDICATOR_SIZE = 16;
    private static final double INDICATOR_GAP = 6;

    @FXML
    private TabPane chatsContainer;

    private Predicate<Chat> archiveHandler;
    private Predicate<Chat> forgetHandler;
    private Tab emptyChat;
    private final Set<Chat> communicating = Collections.newSetFromMap(new IdentityHashMap<>());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void addChat(Node chatNode, String title, Chat chat, ChatPaneController chatController) {
        Tab tab = emptyChat;
        if (tab == null) {
            tab = new Tab(title);
            chatsContainer.getTabs().add(tab);
        } else {
            emptyChat = null;
        }
        tab.setContent(chatNode);
        tab.setUserData(chatController);
        tab.setText(title);
        tab.setGraphic(null);
        setTabActions(tab, title, chat);
    }

    public void addEmptyChat(Node chatNode, String title, ChatPaneController chatController) {
        Tab tab = new Tab(title);
        tab.setContent(chatNode);
        tab.setUserData(chatController);
        setTabActions(tab, title, null);
        chatsContainer.getTabs().add(tab);
        emptyChat = tab;
    }

    private void setTabActions(Tab tab, String title, Chat chat) {
        final RingProgressIndicator ring = activityIndicator();
        ring.setVisible(chat != null && communicating.contains(chat));
        ring.setManaged(ring.isVisible());
        tab.getProperties().put(CHAT_KEY, chat);
        tab.getProperties().put(INDICATOR_KEY, ring);

        HBox header = new HBox(1);
        if (archiveHandler != null) {
            header.getChildren().add(archiveButton(chat, tab));
        }
        if (forgetHandler != null) {
            header.getChildren().add(forgetButton(chat, tab));
        }
        if (header.getChildren().isEmpty()) {
            tab.setGraphic(ring);
            return;
        }
        header.getChildren().addFirst(new Separator(Orientation.VERTICAL));
        header.getChildren().addFirst(new Label(title));
        header.getChildren().addFirst(ring);
        HBox.setMargin(ring, new Insets(0, INDICATOR_GAP, 0, 0));
        header.setAlignment(Pos.CENTER_LEFT);
        tab.setText(null);
        tab.setGraphic(header);
    }

    private RingProgressIndicator activityIndicator() {
        final RingProgressIndicator ring = new RingProgressIndicator(0, false);
        ring.setMinSize(INDICATOR_SIZE, INDICATOR_SIZE);
        ring.setPrefSize(INDICATOR_SIZE, INDICATOR_SIZE);
        ring.setMaxSize(INDICATOR_SIZE, INDICATOR_SIZE);
        ring.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        return ring;
    }

    /**
     * Shows or hides the activity ring in the tab of the given chat. The state is
     * remembered, so that it is kept if the tabs are rebuilt.
     *
     * @param chat the chat whose communication state changed
     * @param active true while a communication is ongoing
     */
    void communicating(Chat chat, boolean active) {
        if (active) {
            communicating.add(chat);
        } else {
            communicating.remove(chat);
        }
        for (Tab tab : chatsContainer.getTabs()) {
            if (tab.getProperties().get(CHAT_KEY) == chat) {
                final Node ring = (Node) tab.getProperties().get(INDICATOR_KEY);
                ring.setVisible(active);
                ring.setManaged(active);
            }
        }
    }

    void archiveHandler(Predicate<Chat> archiveHandler) {
        this.archiveHandler = archiveHandler;
    }

    void forgetHandler(Predicate<Chat> forgetHandler) {
        this.forgetHandler = forgetHandler;
    }

    private Button archiveButton(Chat chat, Tab tab) {
        Button button = iconButton(Feather.MINUS, "chat-tab-archive-button", "Close and archive");
        button.setOnAction(event -> {
            logChatMessages(chat, (ChatPaneController) tab.getUserData());
            if (chat == null || archiveHandler.test(chat)) {
                closeTab(tab);
            }
        });
        return button;
    }

    private void logChatMessages(Chat chat, ChatPaneController chatPane) {
        if (chat == null) {
            return;
        }

        LOG.fine(() -> chatMessageDetails(chat, chatPane));
    }

    private String chatMessageDetails(Chat chat, ChatPaneController chatPane) {
        StringBuilder details = new StringBuilder("Archived chat messages:");
        int index = 1;
        for (ChatMessage message : chat.messages()) {
            details.append(System.lineSeparator())
                .append("Message ").append(index++)
                .append(System.lineSeparator())
                .append("Type: ").append(MessagePane.messageTypeFor(message))
                .append(System.lineSeparator())
                .append("Title/name: ").append(messageName(message))
                .append(System.lineSeparator())
                .append("Pane state: ").append(messagePaneState(chatPane, message))
                .append(System.lineSeparator())
                .append("Content:").append(System.lineSeparator())
                .append(message.content());
        }

        return details.toString();
    }

    private String messagePaneState(ChatPaneController chatPane, ChatMessage message) {
        return chatPane.messagePaneExpanded(message)
            .map(expanded -> expanded ? "unfolded" : "collapsed")
            .orElse("unavailable");
    }

    private String messageName(ChatMessage message) {
        return switch (message) {
            case ToolInvocationMessage invocation -> invocation.invocation.name;
            default -> message.role().name();
        };
    }

    private Button forgetButton(Chat chat, Tab tab) {
        Button button = iconButton(Feather.X, "chat-tab-forget-button", "Close and forget");
        button.setOnAction(event -> confirmForget(chat, tab));
        return button;
    }

    private Button iconButton(Feather icon, String styleClass, String accessibleText) {
        FontIcon fontIcon = FontIcon.of(icon);
        Button button = new Button(null, fontIcon);
        button.getStyleClass().addAll(Styles.FLAT, Styles.BUTTON_CIRCLE, Styles.BUTTON_ICON, styleClass);
        button.setPadding(new Insets(1));
        button.setPrefWidth(18);
        button.setTooltip(new Tooltip(accessibleText));
        button.setAccessibleText(accessibleText);
        button.setFocusTraversable(false);
        return button;
    }

    private void confirmForget(Chat chat, Tab tab) {
        Alert confirmation = new Alert(
            Alert.AlertType.CONFIRMATION,
            "This conversation will be completely discarded. This action cannot be undone.",
            FORGET_BUTTON,
            ButtonType.CANCEL
        );
        confirmation.setTitle("Forget chat");
        confirmation.setHeaderText(null);
        confirmation.setOnHidden(event -> {
            if (FORGET_BUTTON.equals(confirmation.getResult())
                && (chat == null || forgetHandler.test(chat))) {
                closeTab(tab);
            }
        });
        confirmation.show();
    }

    private void closeTab(Tab tab) {
        chatsContainer.getTabs().remove(tab);
        if (emptyChat == tab) {
            emptyChat = null;
        }
    }

    public boolean hasEmptyChat() {
        return emptyChat != null;
    }

    public void selectLast() {
        if (!chatsContainer.getTabs().isEmpty()) {
            chatsContainer.getSelectionModel().selectLast();
        }
    }

    public void clear() {
        chatsContainer.getTabs().clear();
        emptyChat = null;
    }

    public int getChatCount() {
        return chatsContainer.getTabs().size() - (emptyChat == null ? 0 : 1);
    }

    public ChatPaneController getLastChatController() {
        if (chatsContainer.getTabs().isEmpty()) {
            return null;
        }
        Tab lastTab = chatsContainer.getTabs().get(chatsContainer.getTabs().size() - 1);
        return (ChatPaneController) lastTab.getUserData();
    }
}
