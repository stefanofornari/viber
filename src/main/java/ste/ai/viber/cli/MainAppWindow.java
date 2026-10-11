package ste.ai.viber.cli;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;


import java.io.IOException;
import java.io.UncheckedIOException;
import ste.ai.viber.cli.command.CLIOptions;
import ste.llm.chat.model.Conversation;

public class MainAppWindow {

    private final Parent root;
    private final MainAppController controller;

    public MainAppWindow(Conversation conversation, CLIOptions options, Stage owner) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainAppWindow.fxml"));
            this.root = loader.load();
            this.controller = loader.getController();
            controller.initialize(conversation, options, owner);
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
