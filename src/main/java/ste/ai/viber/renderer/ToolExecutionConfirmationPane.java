package ste.ai.viber.renderer;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ste.ai.viber.util.Safe;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ToolExecutionConfirmationPane extends VBox implements Initializable {

    @FXML
    private ToolInvocationPane headerPane;

    @FXML
    private Label confirmationLabel;

    @FXML
    private Button acceptButton;

    @FXML
    private Button rejectButton;

    public ToolExecutionConfirmationPane() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ToolExecutionConfirmationPane.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        setVisible(false);

        acceptButton.setOnAction((e) -> {
            close(true);
        });

        rejectButton.setOnAction((e) -> {
            close(false);
        });
    }

    public void showMessage(final ToolExecutionRequest execution) {
        Safe.requireNonNull(execution, "execution");
        headerPane.toolInvocation(execution);
        confirmationLabel.setText(String.format(confirmationLabel.getText(), execution.name()));
        setVisible(true);
    }

    private void close(final boolean accepted) {
        fireEvent(new ToolConfirmationEvent(accepted));
        setVisible(false);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
