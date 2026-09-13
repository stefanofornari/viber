package ste.ai.toolify;

import atlantafx.base.theme.NordLight;
import ste.ai.toolify.net.TrustAllCertificates;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.stage.Window;

public class Main extends Application {

    public static HostServices services;

    @Override
    public void start(Stage primaryStage) throws Exception {
        TrustAllCertificates.disableCertificateValidation();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ste/ai/toolify/main.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();

        primaryStage.setTitle("AI Toolify");
        primaryStage.setScene(new Scene(root, 800, 600));

        // Set application icon
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("app_icon.png")));

        // Save system prompt on application close
        primaryStage.setOnCloseRequest(event -> {
            controller.saveSystemPrompt();
        });

        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());

        final String bridgeUrl = getClass().getResource("/style/bridge.css").toExternalForm();

        Window.getWindows().addListener((javafx.collections.ListChangeListener.Change<? extends Window> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (Window window : c.getAddedSubList()) {
                        if (window instanceof Stage stage) {
                            // Check if this is the PreferencesFX window (usually by title)
                            // If you didn't set a title, it might be empty or a default.
                            stage.getScene().getStylesheets().add(bridgeUrl);
                            stage.showingProperty().addListener((obs, oldVal, newVal) -> {
                                if (newVal) {
                                    stage.setMinWidth(800);
                                    stage.setMinHeight(600);
                                    stage.sizeToScene();
                                    // Optional: prevent it from getting too small again
                                    stage.setMinWidth(stage.getWidth());
                                    stage.setMinHeight(stage.getHeight());
                                }
                            });
                        }
                    }
                }
            }
        });

        /*
        primaryStage.setOnShown(
            e -> GUI.openToolStage(primaryStage, getHostServices())
        );
        */

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
