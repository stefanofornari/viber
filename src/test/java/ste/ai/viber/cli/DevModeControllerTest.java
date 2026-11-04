package ste.ai.viber.cli;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;
import ste.ai.viber.actor.FXActor;
import ste.ai.viber.renderer.GUIRenderer;
import ste.ai.viber.renderer.QuestionsPane;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.Conversation;
import ste.llm.chat.model.ErrorMessage;
import ste.llm.chat.model.PromptMessage;
import ste.llm.chat.model.ReplyMessage;
import ste.llm.chat.model.ToolExecutionMessage;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class DevModeControllerTest extends org.testfx.framework.junit5.ApplicationTest {

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        ViberApplication.HOST = new javafx.application.HostServices();
        stage.show();
    }

    @Test
    void controller_disables_buttons_when_text_is_blank() {
        DevModeController controller = loadController();
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.addChatButton.isDisabled()).isTrue();
        then(controller.addReplyButton.isDisabled()).isTrue();
    }

    @Test
    void controller_enables_buttons_when_text_is_present() {
        DevModeController controller = loadController();
        WaitForAsyncUtils.waitForFxEvents();

        controller.inputText.setText("hello");
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.addChatButton.isDisabled()).isFalse();
        then(controller.addReplyButton.isDisabled()).isFalse();
    }

    @Test
    void menu_button_is_present_with_correct_items() {
        DevModeController controller = loadController();
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.addAskToolMenuButton).isNotNull();
        then(controller.addAskToolMenuButton.getText()).isEqualTo("Add AskTool");
        then(controller.addAskToolMenuButton.getItems()).hasSize(4);
        then(controller.askQuestionMenuItem.getText()).isEqualTo("AskQuestion");
        then(controller.askSingleChoiceMenuItem.getText()).isEqualTo("AskSingleChoice");
        then(controller.askMultipleChoiceMenuItem.getText()).isEqualTo("AskMultipleChoice");
        then(controller.askConfirmationMenuItem.getText()).isEqualTo("AskConfirmation");
    }

    @Test
    void menu_button_enabled_when_text_is_blank() {
        DevModeController controller = loadController();
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.addAskToolMenuButton.isDisabled()).isFalse();
    }

    @Test
    void menu_button_enabled_when_text_is_present() {
        DevModeController controller = loadController();
        WaitForAsyncUtils.waitForFxEvents();

        controller.inputText.setText("hello");
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.addAskToolMenuButton.isDisabled()).isFalse();
    }

    @Test
    void menu_button_label_updates_when_ask_question_selected() {
        DevModeController controller = loadController();
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> controller.askQuestionMenuItem.fire());
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.addAskToolMenuButton.getText()).isEqualTo("Add AskQuestion");
    }

    @Test
    void menu_button_label_updates_when_ask_single_choice_selected() {
        DevModeController controller = loadController();
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> controller.askSingleChoiceMenuItem.fire());
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.addAskToolMenuButton.getText()).isEqualTo("Add AskSingleChoice");
    }

    @Test
    void menu_button_label_updates_when_ask_multiple_choice_selected() {
        DevModeController controller = loadController();
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> controller.askMultipleChoiceMenuItem.fire());
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.addAskToolMenuButton.getText()).isEqualTo("Add AskMultipleChoice");
    }

    @Test
    void menu_button_label_updates_when_ask_confirmation_selected() {
        DevModeController controller = loadController();
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> controller.askConfirmationMenuItem.fire());
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.addAskToolMenuButton.getText()).isEqualTo("Add AskConfirmation");
    }

    @Test
    void ask_question_menu_item_inserts_template() {
        DevModeController controller = loadController();
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> controller.askQuestionMenuItem.fire());
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.inputText.getText()).isEqualTo("{\"tool\":\"AskQuestion\",\"arguments\":{\"question\":\"Enter your question here\"}}");
    }

    @Test
    void ask_single_choice_menu_item_inserts_template() {
        DevModeController controller = loadController();
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> controller.askSingleChoiceMenuItem.fire());
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.inputText.getText()).isEqualTo("{\"tool\":\"AskSingleChoice\",\"arguments\":{\"question\":\"Enter your question here\",\"options\":[\"Option 1\",\"Option 2\"]}}");
    }

    @Test
    void ask_multiple_choice_menu_item_inserts_template() {
        DevModeController controller = loadController();
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> controller.askMultipleChoiceMenuItem.fire());
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.inputText.getText()).isEqualTo("{\"tool\":\"AskMultipleChoice\",\"arguments\":{\"question\":\"Enter your question here\",\"options\":[\"Option 1\",\"Option 2\"]}}");
    }

    @Test
    void ask_confirmation_menu_item_inserts_template() {
        DevModeController controller = loadController();
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> controller.askConfirmationMenuItem.fire());
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.inputText.getText()).isEqualTo("{\"tool\":\"AskConfirmation\",\"arguments\":{\"question\":\"Enter your question here\"}}");
    }

    @Test
    void valid_ask_question_json_passes_validation() {
        Conversation conversation = new Conversation();
        GUIRenderer renderer = new GUIRenderer();
        FXActor fxActor = new FXActor(conversation, new GUIRenderer());

        DevModeController controller = loadController(fxActor, conversation, renderer, Collections.emptyList());
        WaitForAsyncUtils.waitForFxEvents();

        // First create a chat
        interact(() -> {
            controller.inputText.setText("chat text");
            controller.addChatButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Now test valid AskQuestion JSON with Add Reply
        interact(() -> {
            controller.inputText.setText("{\"tool\":\"AskQuestion\",\"arguments\":{\"question\":\"What is your name?\"}}");
            controller.addReplyButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(conversation.chats()).hasSize(1);
        then(conversation.chats().getFirst().messages()).hasSize(2);
    }

    @Test
    void valid_ask_single_choice_json_passes_validation() {
        Conversation conversation = new Conversation();
        GUIRenderer renderer = new GUIRenderer();
        FXActor fxActor = new FXActor(conversation, renderer);

        DevModeController controller = loadController(fxActor, conversation, renderer, Collections.emptyList());
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("chat text");
            controller.addChatButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("{\"tool\":\"AskSingleChoice\",\"arguments\":{\"question\":\"Where?\",\"options\":[\"A\",\"B\"]}}");
            controller.addThoughtButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(conversation.chats()).hasSize(1);
        then(conversation.chats().getFirst().messages()).hasSize(2);
    }

    @Test
    void valid_ask_multiple_choice_json_passes_validation() {
        Conversation conversation = new Conversation();
        GUIRenderer renderer = new GUIRenderer();
        FXActor fxActor = new FXActor(conversation, renderer);

        DevModeController controller = loadController(fxActor, conversation, renderer, Collections.emptyList());
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("chat text");
            controller.addChatButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("{\"tool\":\"AskMultipleChoice\",\"arguments\":{\"question\":\"Where?\",\"options\":[\"A\",\"B\"]}}");
            controller.addToolReplyButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(conversation.chats()).hasSize(1);
        then(conversation.chats().getFirst().messages()).hasSize(2);
    }

    @Test
    void valid_ask_confirmation_json_passes_validation() {
        Conversation conversation = new Conversation();
        GUIRenderer renderer = new GUIRenderer();
        FXActor fxActor = new FXActor(conversation, renderer);

        DevModeController controller = loadController(fxActor, conversation, renderer, Collections.emptyList());
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("chat text");
            controller.addChatButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("{\"tool\":\"AskConfirmation\",\"arguments\":{\"question\":\"Confirm?\"}}");
            controller.addErrorButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(conversation.chats()).hasSize(1);
        then(conversation.chats().getFirst().messages()).hasSize(2);
    }

    @Test
    void invalid_json_syntax_reports_error() {
        Conversation conversation = new Conversation();
        GUIRenderer renderer = new GUIRenderer();
        FXActor fxActor = new FXActor(conversation, renderer);

        DevModeController controller = loadController(fxActor, conversation, renderer, Collections.emptyList());
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("chat text");
            controller.addChatButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("{invalid json}");
            controller.addReplyButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(conversation.chats().getFirst().messages()).hasSize(2);
        then(conversation.chats().getFirst().messages().getLast()).isInstanceOf(ErrorMessage.class);
        then(conversation.chats().getFirst().messages().getLast().content()).contains("Invalid JSON syntax");
    }

    @Test
    void missing_tool_field_reports_error() {
        Conversation conversation = new Conversation();
        GUIRenderer renderer = new GUIRenderer();
        FXActor fxActor = new FXActor(conversation, renderer);

        DevModeController controller = loadController(fxActor, conversation, renderer, Collections.emptyList());
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("chat text");
            controller.addChatButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("{\"arguments\":{\"question\":\"Test\"}}");
            controller.addReplyButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(conversation.chats().getFirst().messages()).hasSize(2);
        then(conversation.chats().getFirst().messages().getLast()).isInstanceOf(ErrorMessage.class);
        then(conversation.chats().getFirst().messages().getLast().content()).contains("Missing 'tool'");
    }

    @Test
    void invalid_tool_name_reports_error() {
        Conversation conversation = new Conversation();
        GUIRenderer renderer = new GUIRenderer();
        FXActor fxActor = new FXActor(conversation, renderer);

        DevModeController controller = loadController(fxActor, conversation, renderer, Collections.emptyList());
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("chat text");
            controller.addChatButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("{\"tool\":\"InvalidTool\",\"arguments\":{\"question\":\"Test\"}}");
            controller.addReplyButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(conversation.chats().getFirst().messages()).hasSize(2);
        then(conversation.chats().getFirst().messages().getLast()).isInstanceOf(ErrorMessage.class);
        then(conversation.chats().getFirst().messages().getLast().content()).contains("Invalid AskTools tool name");
    }

    @Test
    void missing_arguments_field_reports_error() {
        Conversation conversation = new Conversation();
        GUIRenderer renderer = new GUIRenderer();
        FXActor fxActor = new FXActor(conversation, renderer);

        DevModeController controller = loadController(fxActor, conversation, renderer, Collections.emptyList());
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("chat text");
            controller.addChatButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("{\"tool\":\"AskQuestion\"}");
            controller.addReplyButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(conversation.chats().getFirst().messages()).hasSize(2);
        then(conversation.chats().getFirst().messages().getLast()).isInstanceOf(ErrorMessage.class);
        then(conversation.chats().getFirst().messages().getLast().content()).contains("Missing 'arguments'");
    }

    @Test
    void ask_question_missing_question_field_reports_error() {
        Conversation conversation = new Conversation();
        GUIRenderer renderer = new GUIRenderer();
        FXActor fxActor = new FXActor(conversation, renderer);

        DevModeController controller = loadController(fxActor, conversation, renderer, Collections.emptyList());
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("chat text");
            controller.addChatButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("{\"tool\":\"AskQuestion\",\"arguments\":{}}");
            controller.addReplyButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(conversation.chats().getFirst().messages()).hasSize(2);
        then(conversation.chats().getFirst().messages().getLast()).isInstanceOf(ErrorMessage.class);
        then(conversation.chats().getFirst().messages().getLast().content()).contains("Missing or empty 'question' field");
    }

    @Test
    void ask_single_choice_missing_options_array_reports_error() {
        Conversation conversation = new Conversation();
        GUIRenderer renderer = new GUIRenderer();
        FXActor fxActor = new FXActor(conversation, renderer);

        DevModeController controller = loadController(fxActor, conversation, renderer, Collections.emptyList());
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("chat text");
            controller.addChatButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("{\"tool\":\"AskSingleChoice\",\"arguments\":{\"question\":\"Test\"}}");
            controller.addReplyButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(conversation.chats().getFirst().messages()).hasSize(2);
        then(conversation.chats().getFirst().messages().getLast()).isInstanceOf(ErrorMessage.class);
        then(conversation.chats().getFirst().messages().getLast().content()).contains("Missing 'options' array");
    }

    @Test
    void ask_single_choice_options_not_array_reports_error() {
        Conversation conversation = new Conversation();
        GUIRenderer renderer = new GUIRenderer();
        FXActor fxActor = new FXActor(conversation, renderer);

        DevModeController controller = loadController(fxActor, conversation, renderer, Collections.emptyList());
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("chat text");
            controller.addChatButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("{\"tool\":\"AskSingleChoice\",\"arguments\":{\"question\":\"Test\",\"options\":\"not-array\"}}");
            controller.addReplyButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(conversation.chats().getFirst().messages()).hasSize(2);
        then(conversation.chats().getFirst().messages().getLast()).isInstanceOf(ErrorMessage.class);
        then(conversation.chats().getFirst().messages().getLast().content()).contains("'options' must be an array");
    }

    @Test
    void controller_add_reply_creates_message() {
        Conversation conversation = new Conversation();
        GUIRenderer renderer = new GUIRenderer();
        FXActor fxActor = new FXActor(conversation, renderer);

        DevModeController controller = loadController(fxActor, conversation, renderer, Collections.emptyList());
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("chat text");
            controller.addChatButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputText.setText("reply text");
            controller.addReplyButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(conversation.chats()).hasSize(1);
        then(conversation.chats().getFirst().messages()).hasSize(2);
        then(conversation.chats().getFirst().messages().getFirst().content()).isEqualTo("chat text");
        then(conversation.chats().getFirst().messages().getLast()).isInstanceOf(ReplyMessage.class);
        then(conversation.chats().getFirst().messages().getLast().content()).isEqualTo("reply text");
    }

    @Test
    void manual_and_normal_actor_render_ask_tool_responses_in_the_main_conversation() {
        Conversation conversation = new Conversation();
        GUIRenderer mainRenderer = new GUIRenderer();
        GUIRenderer devModeRenderer = new GUIRenderer();
        FXActor fxActor = new FXActor(conversation, mainRenderer);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DevModeWindow.fxml"));
        Parent root;
        DevModeController controller;
        try {
            root = loader.load();
            controller = loader.getController();
            controller.initialize(fxActor, conversation, devModeRenderer, Collections.emptyList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        QuestionsPane[] shownPane = new QuestionsPane[1];

        fxActor.askToolConsumer(node -> {
            shownPane[0] = (QuestionsPane) node;
            controller.conversationContainer.getChildren().add(node);
        });

        interact(() -> stage.setScene(new Scene(new VBox(mainRenderer, root))));
        WaitForAsyncUtils.waitForFxEvents();

        Chat[] chat = new Chat[1];
        interact(() -> {
            chat[0] = new Chat(new PromptMessage("chat text"));
            fxActor.chat(chat[0], message -> {});
        });
        WaitForAsyncUtils.waitForFxEvents();

        List<AskToolAnswer> cases = List.of(
            new AskToolAnswer(
                "{\"tool\":\"AskQuestion\",\"arguments\":{\"question\":\"What is your name?\"}}",
                "My name",
                "My name"
            ),
            new AskToolAnswer(
                "{\"tool\":\"AskSingleChoice\",\"arguments\":{\"question\":\"Choose one\",\"options\":[\"Alpha\",\"Beta\"]}}",
                "Beta",
                "Beta"
            ),
            new AskToolAnswer(
                "{\"tool\":\"AskMultipleChoice\",\"arguments\":{\"question\":\"Choose all\",\"options\":[\"Alpha\",\"Beta\"]}}",
                "Alpha",
                "[Alpha]"
            ),
            new AskToolAnswer(
                "{\"tool\":\"AskConfirmation\",\"arguments\":{\"question\":\"Continue?\"}}",
                "No",
                "false"
            )
        );

        for (int i = 0; i < cases.size(); i++) {
            AskToolAnswer testCase = cases.get(i);
            interact(() -> {
                controller.inputText.setText(testCase.invocation());
                controller.addToolExecutionButton.fire();
            });
            WaitForAsyncUtils.waitForFxEvents();

            QuestionsPane pane = shownPane[0];
            then(pane).as("AskTool panel for %s; messages: %s", testCase.invocation(), chat[0].messages()).isNotNull();
            interact(() -> {
                if (testCase.invocation().contains("\"AskQuestion\"")) {
                    pane.lookupAll(".question-input").stream()
                        .filter(TextArea.class::isInstance)
                        .map(TextArea.class::cast)
                        .findFirst()
                        .orElseThrow()
                        .setText(testCase.answer());
                } else if (testCase.invocation().contains("\"AskMultipleChoice\"")) {
                    pane.lookupAll(".question-input").stream()
                        .filter(VBox.class::isInstance)
                        .map(VBox.class::cast)
                        .findFirst()
                        .orElseThrow()
                        .lookupAll(".check-box").stream()
                        .filter(CheckBox.class::isInstance)
                        .map(CheckBox.class::cast)
                        .findFirst()
                        .orElseThrow()
                        .setSelected(true);
                } else {
                    pane.lookupAll(".question-input").stream()
                        .filter(ChoiceBox.class::isInstance)
                        .map(ChoiceBox.class::cast)
                        .findFirst()
                        .orElseThrow()
                        .getSelectionModel()
                        .select(testCase.answer());
                }
                ((Button) pane.lookup(".answer-button")).fire();
            });
            WaitForAsyncUtils.waitForFxEvents();

            then(pane.getParent()).isNull();
            then(chat[0].messages().getLast()).isInstanceOf(ToolExecutionMessage.class);
            then(chat[0].messages().getLast().content()).isEqualTo(testCase.expectedResult());
            Parent renderedMainChat = (Parent) mainRenderer.getTabs().getFirst().getContent();
            then(renderedMainChat.lookupAll(".message-tool-response")).hasSize(1);
            then(renderedMainChat.lookupAll(".message-tool")).isEmpty();
            then(devModeRenderer.lookupAll(".message-tool-response")).isEmpty();
        }
    }

    private record AskToolAnswer(String invocation, String answer, String expectedResult) {
    }

    private DevModeController loadController() {
        GUIRenderer renderer = new GUIRenderer();
        FXActor fxActor = new FXActor(new Conversation(), renderer);
        return loadController(fxActor, fxActor.conversation(), renderer, Collections.emptyList());
    }

    private DevModeController loadController(FXActor fxActor, Conversation conversation, GUIRenderer renderer, List<ste.llm.tools.Tool> tools) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DevModeWindow.fxml"));
            Parent root = loader.load();
            DevModeController controller = loader.getController();
            controller.initialize(fxActor, conversation, renderer, tools);
            return controller;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}