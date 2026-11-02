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
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.event.Event;
import ste.llm.chat.model.ToolInvocationMessage;
import ste.llm.tools.InvocationJSONObject;
import org.json.JSONObject;
import static ste.uzzo.agent.tools.AskTools.ASK_QUESTION;
import static ste.uzzo.agent.tools.AskTools.ASK_SINGLE_CHOICE;

class ChatPaneControllerTest extends ApplicationTest {

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        ViberApplication.HOST = new HostServices();
        stage.show();
    }

    @Test
    void mouse_wheel_scrolls_nested_message_content_before_chat() throws Exception {
        ScrollPanes panes = createChatWithNestedContent(1000, 1000);
        then(panes.nested().getVmax()).isGreaterThan(panes.nested().getVmin());
        then(panes.chat().getVmax()).isGreaterThan(panes.chat().getVmin());

        interact(() -> fireScroll(panes.nested().getContent(), 0, -40));

        then(panes.nested().getVvalue()).isGreaterThan(panes.nested().getVmin());
        then(panes.chat().getVvalue()).isEqualTo(panes.chat().getVmin());

        interact(() -> {
            panes.nested().setVvalue(panes.nested().getVmax() - 0.01);
            fireScroll(panes.nested().getContent(), 0, -40);
        });

        then(panes.nested().getVvalue()).isEqualTo(panes.nested().getVmax());
        then(panes.chat().getVvalue()).isEqualTo(panes.chat().getVmin());

        interact(() -> fireScroll(panes.nested().getContent(), 0, -40));

        then(panes.chat().getVvalue()).isGreaterThan(panes.chat().getVmin());
    }

    @Test
    void horizontal_wheel_scrolls_content_without_falling_back_to_vertical() throws Exception {
        ScrollPanes panes = createChatWithNestedContent(1000, 80);
        then(panes.nested().getHmax()).isGreaterThan(panes.nested().getHmin());

        interact(() -> fireScroll(panes.nested().getContent(), 40, 0));

        then(panes.nested().getHvalue()).isGreaterThan(panes.nested().getHmin());
        then(panes.nested().getVvalue()).isEqualTo(panes.nested().getVmin());
        then(panes.chat().getVvalue()).isEqualTo(panes.chat().getVmin());
    }

    @Test
    void horizontal_wheel_is_ignored_when_content_does_not_overflow_horizontally() throws Exception {
        ScrollPanes panes = createChatWithNestedContent(100, 80);

        interact(() -> fireScroll(panes.nested().getContent(), 40, 0));

        then(panes.nested().getHvalue()).isEqualTo(panes.nested().getHmin());
        then(panes.chat().getVvalue()).isEqualTo(panes.chat().getVmin());
    }

    @Test
    void mouse_wheel_scrolls_chat_when_not_over_nested_content() throws Exception {
        ScrollPanes panes = createChatWithNestedContent(1000, 1000);

        interact(() -> fireScroll(panes.chat().getContent(), 0, -40));

        then(panes.chat().getVvalue()).isGreaterThan(panes.chat().getVmin());
        then(panes.nested().getVvalue()).isEqualTo(panes.nested().getVmin());
    }

    @Test
    void mouse_wheel_scrolls_ancestor_scroll_pane_after_chat_reaches_its_limit() throws Exception {
        ScrollPanes[] panes = new ScrollPanes[1];
        interact(() -> {
            try {
                ChatPaneFixture fixture = loadChatPane();
                VBox outerContent = new VBox(fixture.root(), createTallContent());
                ScrollPane outer = new ScrollPane(outerContent);
                outer.setPrefViewportHeight(150);
                stage.setScene(new Scene(outer, 250, 180));
                layout(outer);
                layout(fixture.root());
                panes[0] = new ScrollPanes((ScrollPane) fixture.root(), outer);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        ScrollPane chat = panes[0].chat();
        ScrollPane outer = panes[0].nested();
        then(outer.getVmax()).isGreaterThan(outer.getVmin());

        interact(() -> fireScroll(chat, 0, -40));

        then(chat.getVvalue()).isEqualTo(chat.getVmin());
        then(outer.getVvalue()).isGreaterThan(outer.getVmin());
    }

    private Region createTallContent() {
        Region content = new Region();
        content.setMinSize(150, 600);
        content.setPrefSize(150, 600);
        return content;
    }

    private ScrollPanes createChatWithNestedContent(double contentWidth, double contentHeight) throws Exception {
        ScrollPanes[] result = new ScrollPanes[1];
        interact(() -> {
            try {
                ChatPaneFixture fixture = loadChatPane();
                ScrollPane nested = createScrollablePane(contentWidth, contentHeight);
                nested.setPrefViewportWidth(150);
                nested.setPrefViewportHeight(100);
                nested.setPrefSize(170, 120);
                fixture.controller().addMessage(nested);
                fixture.controller().addMessage(createTallContent());
                stage.setScene(new Scene(fixture.root(), 250, 180));
                layout(fixture.root());
                nested.layout();
                result[0] = new ScrollPanes((ScrollPane) fixture.root(), nested);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return result[0];
    }

    private ChatPaneFixture loadChatPane() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPane.fxml"));
        Parent root = loader.load();
        return new ChatPaneFixture(root, loader.getController());
    }

    private void layout(Parent node) {
        node.applyCss();
        node.layout();
    }

    private ScrollPane createScrollablePane(double width, double height) {
        Region content = new Region();
        content.setMinSize(width, height);
        content.setPrefSize(width, height);
        return new ScrollPane(content);
    }

    private void fireScroll(Node target, double deltaX, double deltaY) {
        Event.fireEvent(target, new ScrollEvent(
            ScrollEvent.SCROLL,
            0, 0, 0, 0,
            false, false, false, false,
            true, false,
            deltaX, deltaY, deltaX, deltaY,
            ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
            ScrollEvent.VerticalTextScrollUnits.NONE, 0,
            0, null
        ));
    }

    private record ScrollPanes(ScrollPane chat, ScrollPane nested) {
    }

    private record ChatPaneFixture(Parent root, ChatPaneController controller) {
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