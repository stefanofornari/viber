package ste.ai.viber.cli;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;
import ste.ai.viber.actor.FXActor;
import ste.ai.viber.cli.ViberApplication;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.ReplyMessage;
import ste.ai.viber.renderer.GUIRenderer;

import static org.assertj.core.api.BDDAssertions.then;

class DevModeControllerTest extends org.testfx.framework.junit5.ApplicationTest {

    @Override
    public void start(Stage stage) {
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
    void controller_add_reply_creates_message() {
        Conversation conversation = new Conversation();
        GUIRenderer renderer = new GUIRenderer();
        FXActor fxActor = new FXActor(conversation, renderer);

        DevModeController controller = loadController(fxActor, conversation, renderer);
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

    private DevModeController loadController() {
        GUIRenderer renderer = new GUIRenderer();
        FXActor fxActor = new FXActor(new Conversation(), renderer);
        return loadController(fxActor, fxActor.conversation(), renderer);
    }

    private DevModeController loadController(FXActor fxActor, Conversation conversation, GUIRenderer renderer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DevModeWindow.fxml"));
            Parent root = loader.load();
            DevModeController controller = loader.getController();
            controller.initialize(fxActor, conversation, renderer);
            return controller;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
