package ste.ai.viber.renderer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.control.Button;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.kordamp.ikonli.javafx.FontIcon;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class ArgumentChip extends HBox implements Initializable {

    public static final String CHIP_SAMPLE_ARGUMENT_NAME = "argument";
    public static final String CHIP_SAMPLE_ARGUMENT_VALUE = "value";

    @FXML
    private Label argumentLabel;

    @FXML
    private Button copyButton;

    @FXML
    private Region ribbon;

    private String value;

    public ArgumentChip() {
        this(CHIP_SAMPLE_ARGUMENT_NAME, CHIP_SAMPLE_ARGUMENT_VALUE);
    }

    public ArgumentChip(final String name, final String value) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ArgumentChip.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        this.value = value;
        argumentLabel.setText(name + ": " + value);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        copyButton.setGraphic(new FontIcon("fth-copy"));
        copyButton.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(value);
            clipboard.setContent(content);

            Tooltip tooltip = new Tooltip("Copied!");
            Bounds bounds = copyButton.localToScreen(copyButton.getBoundsInLocal());
            tooltip.show(copyButton, bounds.getMinX() + bounds.getWidth() / 2 - 20, bounds.getMinY() + bounds.getHeight() + 4);

            PauseTransition pause = new PauseTransition(Duration.millis(1500));
            pause.setOnFinished(event -> tooltip.hide());
            pause.play();
        });
    }
}
