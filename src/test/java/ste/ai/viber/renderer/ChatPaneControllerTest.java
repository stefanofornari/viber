package ste.ai.viber.renderer;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import javafx.application.HostServices;
import javafx.stage.Stage;
import ste.ai.viber.cli.ViberApplication;

import static org.assertj.core.api.BDDAssertions.then;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import ste.llm.chat.model.ToolInvocationMessage;
import ste.llm.tools.InvocationJSONObject;
import org.json.JSONObject;
import static ste.uzzo.agent.tools.AskTools.ASK_QUESTION;
import static ste.uzzo.agent.tools.AskTools.ASK_SINGLE_CHOICE;

class ChatPaneControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        ViberApplication.HOST = new HostServices();
        stage.show();
    }

    @Test
    void ask_tool_invocation_does_not_add_to_messages_container() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPane.fxml"));
        Parent root = loader.load();
        ChatPaneController controller = loader.getController();

        ToolInvocationMessage invocation = new ToolInvocationMessage(
            new InvocationJSONObject(ASK_QUESTION, new JSONObject().put("question", "What is your name?"))
        );

        then(controller.messagesContainerChildren()).isEmpty();

        interact(() -> controller.appendMessage(invocation));
        WaitForAsyncUtils.waitForFxEvents();

        // AskTools should NOT add anything to the messages container
        // (they are displayed in a separate QuestionsPane above the prompt)
        then(controller.messagesContainerChildren()).isEmpty();
    }

    @Test
    void multiple_ask_tool_invocations_do_not_add_to_messages_container() throws Exception {
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

        // AskTools should NOT add anything to the messages container
        then(controller.messagesContainerChildren()).isEmpty();
    }

    @Test
    void regular_tool_invocation_adds_tool_message_pane() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPane.fxml"));
        Parent root = loader.load();
        ChatPaneController controller = loader.getController();

        ToolInvocationMessage invocation = new ToolInvocationMessage(
            new InvocationJSONObject("SomeOtherTool", new JSONObject().put("param", "value"))
        );

        then(controller.messagesContainerChildren()).isEmpty();

        interact(() -> controller.appendMessage(invocation));
        WaitForAsyncUtils.waitForFxEvents();

        // Regular tool invocations should add a ToolMessagePane
        then(controller.messagesContainerChildren()).hasSize(1);
        then(controller.messagesContainerChildren().get(0)).isInstanceOf(ToolMessagePane.class);
    }

    @Test
    void ask_single_choice_invocation_does_not_add_to_messages_container() throws Exception {
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

        // AskTools should NOT add anything to the messages container
        then(controller.messagesContainerChildren()).isEmpty();
    }
}