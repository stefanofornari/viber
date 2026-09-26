package ste.ai.viber.renderer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.Node;

import java.net.URL;
import java.util.ResourceBundle;

public class ConversationPaneController implements Initializable {

    @FXML
    private TabPane chatsContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void addChat(Node chatNode, String title, ChatPaneController chatController) {
        Tab tab = new Tab(title);
        tab.setContent(chatNode);
        tab.setUserData(chatController);
        chatsContainer.getTabs().add(tab);
    }

    public void selectLast() {
        if (!chatsContainer.getTabs().isEmpty()) {
            chatsContainer.getSelectionModel().selectLast();
        }
    }

    public void clear() {
        chatsContainer.getTabs().clear();
    }

    public int getChatCount() {
        return chatsContainer.getTabs().size();
    }

    public ChatPaneController getLastChatController() {
        if (chatsContainer.getTabs().isEmpty()) {
            return null;
        }
        Tab lastTab = chatsContainer.getTabs().get(chatsContainer.getTabs().size() - 1);
        return (ChatPaneController) lastTab.getUserData();
    }
}
