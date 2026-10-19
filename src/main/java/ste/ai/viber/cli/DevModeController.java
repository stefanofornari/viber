package ste.ai.viber.cli;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import ste.ai.viber.actor.FXActor;
import ste.ai.viber.renderer.GUIRenderer;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.Conversation;
import ste.llm.chat.model.PromptMessage;
import ste.llm.tools.Tool;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import static ste.lloop.Loop.on;

public class DevModeController {

    @FXML
    StackPane conversationContainer;

    @FXML
    TextArea inputText;

    @FXML
    Button addChatButton;

    @FXML
    Button addReplyButton;

    @FXML
    Button addThoughtButton;

    @FXML
    Button addToolExecutionButton;

    @FXML
    Button addToolReplyButton;

    @FXML
    Button addErrorButton;

    private FXActor fxActor;
    private GUIRenderer renderer;
    private List<Tool> tools;
    private Conversation conversation;

    public void initialize(FXActor fxActor, Conversation conversation, GUIRenderer renderer, List<Tool> tools) {
        this.fxActor = fxActor;
        this.conversation = conversation;
        this.renderer = renderer;
        this.tools = tools;
        conversationContainer.getChildren().add(renderer);
        renderer.render(conversation);

        addChatButton.setOnAction(e -> handleAddChat());
        addReplyButton.setOnAction(e -> handleAddReply());
        addThoughtButton.setOnAction(e -> handleAddThought());
        addToolExecutionButton.setOnAction(e -> handleAddToolInvocation());
        addToolReplyButton.setOnAction(e -> handleAddToolReply());
        addErrorButton.setOnAction(e -> handleAddError());

        inputText.textProperty().addListener((obs, old, text) -> updateButtonStates());
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean enabled = !inputText.getText().isBlank();
        addChatButton.setDisable(!enabled);
        addReplyButton.setDisable(!enabled);
        addThoughtButton.setDisable(!enabled);
        addToolExecutionButton.setDisable(!enabled);
        addToolReplyButton.setDisable(!enabled);
        addErrorButton.setDisable(!enabled);
    }

    private void handleAddChat() {
        String text = inputText.getText().trim();
        if (text.isBlank()) {
            return;
        }
        fxActor.chat(new Chat(new PromptMessage(text)), msg -> {
        });
        inputText.clear();
    }

    private void handleAddReply() {
        String text = inputText.getText().trim();
        if (text.isBlank()) {
            return;
        }
        fxActor.addReply(text);
        inputText.clear();
    }

    private void handleAddThought() {
        String text = inputText.getText().trim();
        if (text.isBlank()) {
            return;
        }
        fxActor.addThought(text);
        inputText.clear();
    }

    private void handleAddToolInvocation() {
        String text = inputText.getText().trim();
        if (text.isBlank()) {
            return;
        }
        try {
            System.out.println("executing " + text);
            JSONObject json = new JSONObject(text);
            String toolName = json.getString("tool");
            JSONObject argumentsJson = json.getJSONObject("arguments");
            // Convert JSONObject to Map<String, Object> for the tool execution
            Map<String, Object> arguments = argumentsJson.toMap();
            // Find the tool by name using reflection
            for (final Tool t : tools) {
                System.out.println("checking class " + t);
                for(final Method m: t.getClass().getMethods()) {
                    System.out.println("  checking method " + m);
                    for(final Annotation a: m.getDeclaredAnnotations()) {
                        if (a instanceof dev.langchain4j.agent.tool.Tool toolAnnotation) {
                            if (toolName.equals(toolAnnotation.name())) {
                                System.out.println("    found " + m);
                                final List values = new ArrayList();
                                on(m.getParameters()).loop((p) -> {
                                    values.add(arguments.get(p.getName()));
                                });
                                System.out.println("    values: " + values);
                                Object result =  m.invoke(t, values.toArray());
                                String resultString = result == null ? "" : result.toString();
                                fxActor.addToolReply(resultString);
                                inputText.clear();
                                return;
                            }
                        }
                    }
                }
            }

            fxActor.addToolReply("Tool %s not found".formatted(toolName));
            inputText.clear();
        } catch (Exception e) {
            fxActor.addError("Error executing tool: " + e.getMessage());
            inputText.clear();
        }
    }

    private void handleAddToolReply() {
        String text = inputText.getText().trim();
        if (text.isBlank()) {
            return;
        }
        fxActor.addToolReply(text);
        inputText.clear();
    }

    private void handleAddError() {
        String text = inputText.getText().trim();
        if (text.isBlank()) {
            return;
        }
        fxActor.addError(text);
        inputText.clear();
    }
}
