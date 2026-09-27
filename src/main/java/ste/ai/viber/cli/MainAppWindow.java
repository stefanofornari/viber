package ste.ai.viber.cli;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import ste.ai.viber.actor.Actor;
import ste.ai.viber.cli.command.CLIOptions;
import ste.ai.viber.model.Conversation;

import java.io.IOException;
import java.io.UncheckedIOException;

public class MainAppWindow {

    private final Parent root;
    private final MainAppController controller;

    public MainAppWindow(Conversation conversation, CLIOptions options, Actor actor, Stage owner) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainAppWindow.fxml"));
            this.root = loader.load();
            this.controller = loader.getController();
            controller.initialize(conversation, options, actor, owner);
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
}
