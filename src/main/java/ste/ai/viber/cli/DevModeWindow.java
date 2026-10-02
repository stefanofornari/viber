package ste.ai.viber.cli;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import ste.ai.viber.actor.FXActor;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.renderer.GUIRenderer;

import java.io.IOException;
import java.io.UncheckedIOException;

public class DevModeWindow {

    private final FXActor fxActor;
    private final Conversation conversation;
    private final Stage stage;
    private final GUIRenderer renderer;

    public DevModeWindow(FXActor fxActor, Conversation conversation, Stage owner) {
        this.fxActor = fxActor;
        this.conversation = conversation;
        this.stage = new Stage();
        this.renderer = new GUIRenderer();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DevModeWindow.fxml"));
            Parent root = loader.load();
            DevModeController controller = loader.getController();
            controller.initialize(fxActor, conversation, renderer);
            stage.setScene(new javafx.scene.Scene(root, 800, 600));
            stage.setTitle("Viber - Dev Mode");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        stage.setOnShown(e -> positionNextTo(owner));
    }

    private void positionNextTo(Stage owner) {
        double mainX = owner.getX();
        double mainY = owner.getY();
        double mainWidth = owner.getWidth();
        double mainHeight = owner.getHeight();
        double devWidth = stage.getWidth();
        double devHeight = stage.getHeight();
        double totalWidth = mainWidth + devWidth;
        double totalHeight = Math.max(mainHeight, devHeight);
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

        double startX = (screenWidth - totalWidth) / 2;
        double startY = mainY + (mainHeight - totalHeight) / 2;

        owner.setX(startX);
        owner.setY(startY);
        stage.setX(startX + mainWidth);
        stage.setY(startY);
    }

    public void show() {
        stage.show();
    }

    public void hide() {
        stage.hide();
    }

    public Stage getStage() {
        return stage;
    }
}
