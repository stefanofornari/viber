package ste.ai.viber.renderer;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.assertj.core.api.BDDAssertions.then;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import ste.llm.chat.model.ToolInvocationMessage;
import ste.llm.tools.InvocationJSONObject;
import org.json.JSONObject;

class ChatPaneControllerTest extends ApplicationTest {

    @Test
    void handle_ask_tool_invocation_adds_panel_only_once() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPane.fxml"));
        Parent root = loader.load();
        ChatPaneController controller = loader.getController();

        ToolInvocationMessage invocation = new ToolInvocationMessage(
            new InvocationJSONObject("AskQuestion", new JSONObject().put("question", "What is your name?"))
        );

        then(controller.messagesContainerChildren()).isEmpty();

        interact(() -> controller.appendMessage(invocation));
        WaitForAsyncUtils.waitForFxEvents();

        long questionsPanelCount = controller.messagesContainerChildren().stream()
            .filter(node -> node instanceof QuestionsPanel)
            .count();

        then(questionsPanelCount).isEqualTo(1);
    }

    @Test
    void multiple_ask_tool_invocations_reuse_same_panel() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPane.fxml"));
        Parent root = loader.load();
        ChatPaneController controller = loader.getController();

        ToolInvocationMessage invocation1 = new ToolInvocationMessage(
            new InvocationJSONObject("AskQuestion", new JSONObject().put("question", "Q1?"))
        );
        interact(() -> controller.appendMessage(invocation1));
        WaitForAsyncUtils.waitForFxEvents();

        ToolInvocationMessage invocation2 = new ToolInvocationMessage(
            new InvocationJSONObject("AskSingleChoice", new JSONObject()
                .put("question", "Q2?")
                .put("options", new String[]{"A", "B"})
            )
        );
        interact(() -> controller.appendMessage(invocation2));
        WaitForAsyncUtils.waitForFxEvents();

        // Should reuse the same panel, so only one QuestionsPanel in the chat
        long questionsPanelCount = controller.messagesContainerChildren().stream()
            .filter(node -> node instanceof QuestionsPanel)
            .count();

        then(questionsPanelCount).isEqualTo(1);

        // But the panel should have both questions
        QuestionsPanel panel = controller.messagesContainerChildren().stream()
            .filter(node -> node instanceof QuestionsPanel)
            .map(node -> (QuestionsPanel) node)
            .findFirst()
            .orElseThrow(() -> new AssertionError("No QuestionsPanel found"));

        then(panel.questionCount()).isEqualTo(2);
    }

    @Test
    void ask_tool_invocation_adds_question_to_panel() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPane.fxml"));
        Parent root = loader.load();
        ChatPaneController controller = loader.getController();

        ToolInvocationMessage invocation = new ToolInvocationMessage(
            new InvocationJSONObject("AskQuestion", new JSONObject().put("question", "What is your name?"))
        );
        interact(() -> controller.appendMessage(invocation));
        WaitForAsyncUtils.waitForFxEvents();

        // Find the QuestionsPanel and verify it has the question
        QuestionsPanel panel = controller.messagesContainerChildren().stream()
            .filter(node -> node instanceof QuestionsPanel)
            .map(node -> (QuestionsPanel) node)
            .findFirst()
            .orElseThrow(() -> new AssertionError("No QuestionsPanel found"));

        // The panel should have at least 1 question
        then(panel.questionCount()).isEqualTo(1);
    }

    @Test
    void ask_single_choice_invocation_adds_choice_question_to_panel() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPane.fxml"));
        Parent root = loader.load();
        ChatPaneController controller = loader.getController();

        ToolInvocationMessage invocation = new ToolInvocationMessage(
            new InvocationJSONObject("AskSingleChoice", new JSONObject()
                .put("question", "Choose platform")
                .put("options", new String[]{"Web", "Mobile", "Desktop"})
            )
        );
        interact(() -> controller.appendMessage(invocation));
        WaitForAsyncUtils.waitForFxEvents();

        QuestionsPanel panel = controller.messagesContainerChildren().stream()
            .filter(node -> node instanceof QuestionsPanel)
            .map(node -> (QuestionsPanel) node)
            .findFirst()
            .orElseThrow(() -> new AssertionError("No QuestionsPanel found"));

        then(panel.questionCount()).isEqualTo(1);
    }
}