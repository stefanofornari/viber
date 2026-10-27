package ste.ai.viber.cli;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import ste.ai.viber.actor.FXActor;
import ste.ai.viber.renderer.GUIRenderer;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.Conversation;
import ste.llm.chat.model.PromptMessage;
import ste.llm.tools.Tool;
import org.json.JSONException;
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

    @FXML
    MenuButton addAskToolMenuButton;

    @FXML
    MenuItem askQuestionMenuItem;

    @FXML
    MenuItem askSingleChoiceMenuItem;

    @FXML
    MenuItem askMultipleChoiceMenuItem;

    @FXML
    MenuItem askConfirmationMenuItem;

    private FXActor fxActor;
    private GUIRenderer renderer;
    private List<Tool> tools;
    private Conversation conversation;
    private String selectedAskTool = null;

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

        // Set up AskTool menu items to update selection and prefill JSON
        askQuestionMenuItem.setOnAction(e -> selectAskTool("AskQuestion", "{\"tool\":\"AskQuestion\",\"arguments\":{\"question\":\"Enter your question here\"}}"));
        askSingleChoiceMenuItem.setOnAction(e -> selectAskTool("AskSingleChoice", "{\"tool\":\"AskSingleChoice\",\"arguments\":{\"question\":\"Enter your question here\",\"options\":[\"Option 1\",\"Option 2\"]}}"));
        askMultipleChoiceMenuItem.setOnAction(e -> selectAskTool("AskMultipleChoice", "{\"tool\":\"AskMultipleChoice\",\"arguments\":{\"question\":\"Enter your question here\",\"options\":[\"Option 1\",\"Option 2\"]}}"));
        askConfirmationMenuItem.setOnAction(e -> selectAskTool("AskConfirmation", "{\"tool\":\"AskConfirmation\",\"arguments\":{\"question\":\"Enter your question here\"}}"));

        // Pressing the main part of the (split) menu button renders the selected AskTool pane
        addAskToolMenuButton.setOnAction(e -> handleAddToolInvocation());

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
        // MenuButton is always enabled so user can select AskTool type
        addAskToolMenuButton.setDisable(false);
    }

    private void selectAskTool(String toolName, String jsonTemplate) {
        selectedAskTool = toolName;
        addAskToolMenuButton.setText("Add " + toolName);
        inputText.setText(jsonTemplate);
    }

    /**
     * Validates tool invocation JSON in the input text.
     * If the text contains JSON with a "tool" field, validates its structure.
     * For AskTools tools, validates AskTools-specific fields.
     * If invalid, reports error via fxActor.addError() and returns false.
     * If valid or not tool invocation JSON, returns true.
     */
    private boolean validateAskToolsJson(String text) {
        // Only validate if text looks like JSON object
        String trimmed = text.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return true;
        }
        
        try {
            JSONObject json = new JSONObject(trimmed);
            String toolName = json.optString("tool", "");
            boolean hasToolField = json.has("tool");
            boolean hasArgumentsField = json.has("arguments");
            
            // If it has a tool field, validate it's a known tool and has arguments
            if (hasToolField) {
                if (toolName.isBlank()) {
                    fxActor.addError("Missing or empty 'tool' field in tool invocation");
                    return false;
                }
                
                // Validate arguments object exists
                if (!hasArgumentsField) {
                    fxActor.addError("Missing 'arguments' object in tool invocation");
                    return false;
                }
                
                JSONObject arguments = json.getJSONObject("arguments");
                
                // If it's an AskTools tool, validate AskTools-specific fields
                if (isAskToolsTool(toolName)) {
                    // Validate required fields based on tool type
                    switch (toolName) {
                        case "AskQuestion":
                        case "AskConfirmation":
                            if (!arguments.has("question") || arguments.getString("question").isBlank()) {
                                fxActor.addError("Missing or empty 'question' field for " + toolName);
                                return false;
                            }
                            break;
                        case "AskSingleChoice":
                        case "AskMultipleChoice":
                            if (!arguments.has("question") || arguments.getString("question").isBlank()) {
                                fxActor.addError("Missing or empty 'question' field for " + toolName);
                                return false;
                            }
                            if (!arguments.has("options")) {
                                fxActor.addError("Missing 'options' array for " + toolName);
                                return false;
                            }
                            Object options = arguments.get("options");
                            if (!(options instanceof org.json.JSONArray)) {
                                fxActor.addError("'options' must be an array for " + toolName);
                                return false;
                            }
                            break;
                        default:
                            // Known tool but not AskTools - basic validation passed
                            break;
                    }
                } else if (looksLikeAskToolsInvocation(arguments)) {
                    // Tool name is not an AskTools tool but the arguments look like AskTools
                    fxActor.addError("Invalid AskTools tool name: " + toolName);
                    return false;
                }
                // For non-AskTools tools, basic validation (tool + arguments) is enough
                return true;
            }
            
            // If it has arguments but no tool field, it's likely an incomplete tool invocation
            if (hasArgumentsField) {
                fxActor.addError("Missing 'tool' field in tool invocation");
                return false;
            }
            
            // Not a tool invocation JSON, allow it
            return true;
        } catch (JSONException e) {
            fxActor.addError("Invalid JSON syntax: " + e.getMessage());
            return false;
        }
    }
    
    private boolean isAskToolsTool(String toolName) {
        return "AskQuestion".equals(toolName)
            || "AskSingleChoice".equals(toolName)
            || "AskMultipleChoice".equals(toolName)
            || "AskConfirmation".equals(toolName);
    }
    
    private boolean looksLikeAskToolsInvocation(JSONObject arguments) {
        return arguments.has("question") || arguments.has("options");
    }

    private void handleAddChat() {
        String text = inputText.getText().trim();
        if (text.isBlank()) {
            return;
        }
        // Add Chat creates a new chat, so no validation needed for AskTools JSON
        fxActor.chat(new Chat(new PromptMessage(text)), msg -> {
        });
        inputText.clear();
    }

    private void handleAddReply() {
        String text = inputText.getText().trim();
        if (text.isBlank()) {
            return;
        }
        if (!validateAskToolsJson(text)) {
            inputText.clear();
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
        if (!validateAskToolsJson(text)) {
            inputText.clear();
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
        if (!validateAskToolsJson(text)) {
            inputText.clear();
            return;
        }
        
        try {
            JSONObject json = new JSONObject(text);
            String toolName = json.getString("tool");
            
            // If it's an AskTools tool, delegate to FXActor which will render it in the main chat window
            if (isAskToolsTool(toolName)) {
                fxActor.addToolInvocation(text);
                inputText.clear();
                return;
            }
            
            // Otherwise execute as a regular tool via reflection
            System.out.println("executing " + text);
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
        if (!validateAskToolsJson(text)) {
            inputText.clear();
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
        if (!validateAskToolsJson(text)) {
            inputText.clear();
            return;
        }
        fxActor.addError(text);
        inputText.clear();
    }

    @FXML
    private void handleAddAskQuestion() {
        selectAskTool("AskQuestion", "{\"tool\":\"AskQuestion\",\"arguments\":{\"question\":\"Enter your question here\"}}");
    }

    @FXML
    private void handleAddAskSingleChoice() {
        selectAskTool("AskSingleChoice", "{\"tool\":\"AskSingleChoice\",\"arguments\":{\"question\":\"Enter your question here\",\"options\":[\"Option 1\",\"Option 2\"]}}");
    }

    @FXML
    private void handleAddAskMultipleChoice() {
        selectAskTool("AskMultipleChoice", "{\"tool\":\"AskMultipleChoice\",\"arguments\":{\"question\":\"Enter your question here\",\"options\":[\"Option 1\",\"Option 2\"]}}");
    }

    @FXML
    private void handleAddAskConfirmation() {
        selectAskTool("AskConfirmation", "{\"tool\":\"AskConfirmation\",\"arguments\":{\"question\":\"Enter your question here\"}}");
    }
}
