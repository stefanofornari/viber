package ste.ai.viber.renderer;

import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;
import ste.ai.viber.cli.ViberApplication;

import java.util.concurrent.CyclicBarrier;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import javafx.scene.control.TabPane;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.ChatMessage;
import ste.llm.chat.model.Conversation;
import ste.llm.chat.model.PromptMessage;
import ste.llm.chat.model.ReplyMessage;
import ste.llm.chat.model.ThoughtMessage;
import ste.llm.chat.model.ToolExecutionMessage;
import ste.llm.chat.model.ToolInvocationMessage;

class GUIRendererTest extends org.testfx.framework.junit5.ApplicationTest {

    @Override
    public void start(Stage stage) {
        ViberApplication.HOST = new javafx.application.HostServices();
        stage.show();
    }

    @Test
    void renders_conversation_with_one_chat() throws Exception {
        GUIRenderer renderer = new GUIRenderer();
        Chat chat = new Chat(new PromptMessage("Hello"));
        chat.addMessage(new ReplyMessage("Hi there"));
        Conversation conversation = new Conversation().addChat(chat);

        interact(() -> {
            renderer.render(conversation);
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(renderer).isNotNull();
        ScrollPane scrollPane = (ScrollPane) renderer;
        TabPane tabPane = (TabPane) scrollPane.getContent();
        then(tabPane).isNotNull();
        then(tabPane.getTabs()).hasSize(1);
    }

    @Test
    void renders_conversation_with_multiple_chats() throws Exception {
        GUIRenderer renderer = new GUIRenderer();
        Chat chat1 = new Chat(new PromptMessage("A"));
        Chat chat2 = new Chat(new PromptMessage("B"));
        Conversation conversation = new Conversation()
            .addChat(chat1)
            .addChat(chat2);

        interact(() -> {
            renderer.render(conversation);
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(renderer).isNotNull();
        ScrollPane scrollPane = (ScrollPane) renderer;
        TabPane tabPane = (TabPane) scrollPane.getContent();
        then(tabPane).isNotNull();
        then(tabPane.getTabs()).hasSize(2);
    }

    @Test
    void renders_conversation_with_different_message_types() throws Exception {
        GUIRenderer renderer = new GUIRenderer();
        Chat chat = new Chat(new PromptMessage("prompt"));
        chat.addMessage(new ReplyMessage("reply"));
        chat.addMessage(new ThoughtMessage("thought"));
        chat.addMessage(new ToolInvocationMessage("tool(args)"));
        chat.addMessage(new ToolExecutionMessage("result"));
        Conversation conversation = new Conversation().addChat(chat);

        interact(() -> {
            renderer.render(conversation);
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(renderer).isNotNull();
    }

    @Test
    void renders_empty_conversation() throws Exception {
        GUIRenderer renderer = new GUIRenderer();
        Conversation conversation = new Conversation();

        interact(() -> {
            renderer.render(conversation);
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(renderer).isNotNull();
    }

    @Test
    void render_throws_on_null_conversation() {
        GUIRenderer renderer = new GUIRenderer();
        thenThrownBy(() -> renderer.render((Conversation) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("conversation must not be null");
    }

    @Test
    void renderChat_throws_on_null_chat() {
        GUIRenderer renderer = new GUIRenderer();
        thenThrownBy(() -> renderer.render((Chat) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("chat must not be null");
    }

    @Test
    void renderMessage_throws_on_null_message() {
        GUIRenderer renderer = new GUIRenderer();
        thenThrownBy(() -> renderer.render((ChatMessage) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("message must not be null");
    }

    @Test
    void concurrent_modification_when_messages_added_during_render() throws Exception {
        GUIRenderer renderer = new GUIRenderer();
        Chat chat = new Chat(new PromptMessage("Hello"));

        interact(() -> renderer);

        for (int attempt = 0; attempt < 50; attempt++) {
            CyclicBarrier barrier = new CyclicBarrier(2);

            Thread adder = new Thread(() -> {
                try {
                    barrier.await();
                    for (int i = 0; i < 5000; i++) {
                        chat.addMessage(new PromptMessage("msg-" + i));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            adder.setDaemon(true);
            adder.start();

            interact(() -> {
                try {
                    barrier.await();
                    for (int i = 0; i < 500; i++) {
                        int count = 0;
                        for (ChatMessage msg : chat.messages()) {
                            count++;
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            adder.join(10_000);
        }
    }
}
