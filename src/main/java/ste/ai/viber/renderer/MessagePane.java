package ste.ai.viber.renderer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import java.io.IOException;
import java.io.UncheckedIOException;
import ste.ai.viber.model.ChatMessage;

public class MessagePane {

    private final MessagePaneController controller;
    private final Node root;

    public MessagePane() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MessagePane.fxml"));
        try {
            loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        this.controller = loader.getController();
        this.root = loader.getRoot();
    }

    public static String messageTypeFor(ChatMessage message) {
        return MessagePaneController.typeNameFor(message);
    }

    public void setMessage(ChatMessage message) {
        controller.setMessage(message);
    }

    public void appendMessage(ChatMessage message) {
        controller.appendMessage(message);
    }

    public Node getRoot() {
        return root;
    }
}