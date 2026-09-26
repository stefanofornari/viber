package ste.ai.viber.cli;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ste.ai.viber.actor.Actor;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.renderer.JavaFxRenderer;
import ste.ai.viber.renderer.Renderer;

import java.io.IOException;
import java.io.UncheckedIOException;

public class MainAppWindow {

    private final Parent root;
    private final MainAppController controller;
    private final JavaFxRenderer renderer;

    public MainAppWindow(Actor actor, Conversation conversation, JavaFxRenderer renderer) {
        this.renderer = renderer;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainAppWindow.fxml"));
            this.root = loader.load();
            this.controller = loader.getController();
            controller.initialize(actor, conversation, renderer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public MainAppController getController() {
        return controller;
    }

    public JavaFxRenderer getRenderer() {
        return renderer;
    }
}
