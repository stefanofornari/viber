package ste.ai.viber.cli;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;
import ste.ai.viber.actor.Actor;
import ste.ai.viber.actor.FXActor;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.renderer.JavaFxRenderer;

import static org.assertj.core.api.BDDAssertions.then;

class MainAppControllerTest extends org.testfx.framework.junit5.ApplicationTest {

    @Override
    public void start(Stage stage) {
        ViberApplication.HOST = new javafx.application.HostServices();
        stage.show();
    }

    @Test
    void controller_send_creates_prompt_message() {
        Conversation conversation = new Conversation();
        JavaFxRenderer renderer = new JavaFxRenderer();
        Actor actor = new FXActor(conversation, renderer);

        MainAppController controller = loadController(actor, conversation, renderer);
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputField.setText("hello world");
            controller.inputField.fireEvent(new KeyEvent(
                KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, true, false, false
            ));
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(conversation.chats()).hasSize(1);
        then(conversation.chats().getFirst().messages()).hasSize(1);
        then(conversation.chats().getFirst().messages().getFirst()).isInstanceOf(PromptMessage.class);
        then(conversation.chats().getFirst().messages().getFirst().content()).isEqualTo("hello world");
    }

    @Test
    void controller_ctrl_enter_sends_prompt() {
        Conversation conversation = new Conversation();
        JavaFxRenderer renderer = new JavaFxRenderer();
        Actor actor = new FXActor(conversation, renderer);

        MainAppController controller = loadController(actor, conversation, renderer);
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputField.setText("hello ctrl+enter");
            controller.inputField.fireEvent(new KeyEvent(
                KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, true, false, false
            ));
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(conversation.chats()).hasSize(1);
        then(conversation.chats().getFirst().messages().getFirst().content()).isEqualTo("hello ctrl+enter");
    }

    @Test
    void controller_empty_input_does_not_create_message() {
        Conversation conversation = new Conversation();
        JavaFxRenderer renderer = new JavaFxRenderer();
        Actor actor = new FXActor(conversation, renderer);

        MainAppController controller = loadController(actor, conversation, renderer);
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputField.setText("   ");
            controller.inputField.fireEvent(new KeyEvent(
                KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, true, false, false
            ));
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(conversation.chats()).isEmpty();
    }

    @Test
    void controller_input_cleared_after_send() {
        Conversation conversation = new Conversation();
        JavaFxRenderer renderer = new JavaFxRenderer();
        Actor actor = new FXActor(conversation, renderer);

        MainAppController controller = loadController(actor, conversation, renderer);
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            controller.inputField.setText("hello");
            controller.inputField.fireEvent(new KeyEvent(
                KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, true, false, false
            ));
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.inputField.getText()).isEmpty();
    }

    private MainAppController loadController() {
        Conversation conversation = new Conversation();
        JavaFxRenderer renderer = new JavaFxRenderer();
        Actor actor = new FXActor(conversation, renderer);
        return loadController(actor, conversation, renderer);
    }

    private MainAppController loadController(Actor actor, Conversation conversation, JavaFxRenderer renderer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainAppWindow.fxml"));
            Parent root = loader.load();
            MainAppController controller = loader.getController();
            controller.initialize(actor, conversation, renderer);
            return controller;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
