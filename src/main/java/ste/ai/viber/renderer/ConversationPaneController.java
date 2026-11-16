package ste.ai.viber.renderer;

import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import ste.llm.chat.model.Chat;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;

public class ConversationPaneController implements Initializable {

    private static final ButtonType FORGET_BUTTON = new ButtonType("Forget", ButtonBar.ButtonData.OK_DONE);

    @FXML
    private TabPane chatsContainer;

    private Predicate<Chat> archiveHandler;
    private Predicate<Chat> forgetHandler;
    private Tab emptyChat;

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
        HBox header = new HBox(1);
        if (archiveHandler != null) {
            header.getChildren().add(archiveButton(chat, tab));
        }
        if (forgetHandler != null) {
            header.getChildren().add(forgetButton(chat, tab));
        }
        if (!header.getChildren().isEmpty()) {
            header.getChildren().addFirst(new Separator(Orientation.VERTICAL));
            header.getChildren().addFirst(new Label(title));
            tab.setText(null);
            tab.setGraphic(header);
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
            if (chat == null || archiveHandler.test(chat)) {
                closeTab(tab);
            }
        });
        return button;
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
