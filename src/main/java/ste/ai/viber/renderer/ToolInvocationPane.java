package ste.ai.viber.renderer;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import ste.lazyjava.safe.Safe;

import static ste.lloop.Loop.on;

public class ToolInvocationPane extends VBox implements Initializable, Safe {

    public static final String ARGUMENT_SAMPLE_TOOL_NAME = "toolName";

    @FXML
    protected Label nameLabel;

    @FXML
    protected FlowPane argumentsPanel;

    @FXML
    protected ScrollPane argumentsScrollPane;

    private String toolName = "";

    public ToolInvocationPane() {
        this(ARGUMENT_SAMPLE_TOOL_NAME, Map.of(ArgumentChip.CHIP_SAMPLE_ARGUMENT_NAME, ArgumentChip.CHIP_SAMPLE_ARGUMENT_VALUE));
    }

    public ToolInvocationPane(final String name, final Map<String, Object> arguments) {
        requireNonBlank(name, "name");

        loadFxml();
        nameLabel.setText(name);
        on(arguments).loop((argument, value) -> {
            argumentsPanel.getChildren().add(new ArgumentChip(argument, String.valueOf(value)));
        });
        updateArgumentsVisibility();
    }

    public ToolInvocationPane(final ToolExecutionRequest execution) {
        requireNonNull(execution, "execution");
        loadFxml();
        nameLabel.setText(execution.name());
        JSONObject arguments = new JSONObject(execution.arguments());
        on(arguments.keySet()).loop((key) -> {
            argumentsPanel.getChildren().add(new ArgumentChip(key, String.valueOf(arguments.get(key))));
        });
        updateArgumentsVisibility();
    }

    public ToolInvocationPane(final String rawContent) {
        loadFxml();
        rawContent(rawContent);
    }

    public void rawContent(String rawContent) {
        nameLabel.setText(rawContent);
        argumentsPanel.getChildren().clear();

        String toolName = rawContent;
        String argsString = "";

        int parenIndex = rawContent.indexOf('(');
        if (parenIndex > 0 && rawContent.endsWith(")")) {
            toolName = rawContent.substring(0, parenIndex);
            argsString = rawContent.substring(parenIndex + 1, rawContent.length() - 1);
        } else {
            int braceIndex = rawContent.indexOf('{');
            if (braceIndex > 0 && rawContent.endsWith("}")) {
                toolName = rawContent.substring(0, braceIndex).trim();
                argsString = rawContent.substring(braceIndex + 1, rawContent.length() - 1);
            }
        }

        this.toolName = toolName;
        nameLabel.setText(toolName);

        if (!argsString.isBlank()) {
            List<String> argList = splitArgsPreservingQuotedStrings(argsString);
            for (String arg : argList) {
                if (!arg.isBlank()) {
                    int eqIndex = arg.indexOf('=');
                    if (eqIndex < 0) {
                        eqIndex = arg.indexOf(':');
                    }
                    if (eqIndex > 0) {
                        String key = arg.substring(0, eqIndex).trim();
                        String value = arg.substring(eqIndex + 1).trim();
                        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        if (key.length() >= 2 && key.startsWith("\"") && key.endsWith("\"")) {
                            key = key.substring(1, key.length() - 1);
                        }
                        argumentsPanel.getChildren().add(new ArgumentChip(key, value));
                    } else {
                        argumentsPanel.getChildren().add(new ArgumentChip("arg", arg.trim()));
                    }
                }
            }
        }
        updateArgumentsVisibility();
    }

    private List<String> splitArgsPreservingQuotedStrings(String argsString) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;
        for (int i = 0; i < argsString.length(); i++) {
            char c = argsString.charAt(i);
            if (c == '"' || c == '\'') {
                if (!inQuotes) {
                    inQuotes = true;
                    quoteChar = c;
                } else if (quoteChar == c) {
                    inQuotes = false;
                    quoteChar = 0;
                }
                current.append(c);
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result;
    }

    public String toolName() {
        return toolName;
    }

    public void toolInvocation(final ToolExecutionRequest execution) {
        requireNonNull(execution, "execution");
        nameLabel.setText(execution.name());
        argumentsPanel.getChildren().clear();
        JSONObject arguments = new JSONObject(execution.arguments());
        on(arguments.keySet()).loop((key) -> {
            argumentsPanel.getChildren().add(new ArgumentChip(key, String.valueOf(arguments.get(key))));
        });
        updateArgumentsVisibility();
    }

    public void updateArgumentsVisibility() {
        boolean hasArguments = !argumentsPanel.getChildren().isEmpty();
        argumentsScrollPane.setManaged(hasArguments);
        argumentsScrollPane.setVisible(hasArguments);
    }

    public void hideName() {
        nameLabel.setVisible(false);
        nameLabel.setManaged(false);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    private void loadFxml() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ToolInvocationPane.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
