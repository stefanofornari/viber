package ste.ai.viber.renderer;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import ste.ai.viber.util.Safe;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ResourceBundle;
import org.apache.commons.lang3.StringUtils;

public class ToolExecutionPane extends VBox implements Initializable {

    @FXML
    private ToolInvocationPane toolInvocationPane;

    @FXML
    private ScrollPane resultScrollPane;

    @FXML
    private TextArea resultTextArea;

    public ToolExecutionPane(final ToolExecutionRequest execution, final String result) {
        Safe.requireNonNull(execution, "execution");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("ToolExecutionPane.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        toolInvocationPane.toolInvocation(execution);
        resultTextArea.setText(StringUtils.defaultIfBlank(result, "result"));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
