package ste.ai.viber.renderer;

import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;
import ste.ai.viber.cli.ViberApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import javafx.scene.control.TabPane;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.json.JSONObject;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.ChatMessage;
import ste.llm.chat.model.Conversation;
import ste.llm.chat.model.PromptMessage;
import ste.llm.chat.model.ReplyMessage;
import ste.llm.chat.model.ThoughtMessage;
import ste.llm.chat.model.ToolExecutionMessage;
import ste.llm.chat.model.ToolInvocationMessage;
import ste.llm.tools.InvocationJSONObject;

@org.junit.jupiter.api.extension.ExtendWith(ste.ai.viber.cli.TestNameLogger.class)
class GUIRendererTest extends org.testfx.framework.junit5.ApplicationTest {

    @Override
    public void start(Stage stage) {
        ViberApplication.HOST = new javafx.application.HostServices();
        stage.setScene(ste.ai.viber.test.FXTestSupport.styled(new Scene(new StackPane())));
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
        then(renderer).isInstanceOf(TabPane.class);
        then(renderer.getTabs()).hasSize(1);
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
        then(renderer).isInstanceOf(TabPane.class);
        then(renderer.getTabs()).hasSize(2);
    }

    @Test
    void renders_conversation_with_different_message_types() throws Exception {
        GUIRenderer renderer = new GUIRenderer();
        Chat chat = new Chat(new PromptMessage("prompt"));
        chat.addMessage(new ReplyMessage("reply"));
        chat.addMessage(new ThoughtMessage("thought"));
        chat.addMessage(new ToolInvocationMessage(new InvocationJSONObject("tool", new JSONObject("{\"args\":\"\"}"))));
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
        then(renderer.getTabs()).hasSize(1);
        then(renderer.getTabs().get(0).getText()).isEqualTo("Chat 1");
        then(((ChatPaneController) renderer.getTabs().get(0).getUserData()).messagesContainerChildren()).isEmpty();
    }

    @Test
    void first_chat_reuses_the_empty_chat_tab() throws Exception {
        GUIRenderer renderer = new GUIRenderer();
        interact(() -> renderer.render(new Conversation()));
        WaitForAsyncUtils.waitForFxEvents();
        Tab emptyTab = renderer.getTabs().get(0);

        Chat chat = new Chat(new PromptMessage("Hello"));
        interact(() -> renderer.render(chat));
        WaitForAsyncUtils.waitForFxEvents();

        then(renderer.getTabs()).containsExactly(emptyTab);
        then(emptyTab.getText()).isEqualTo("Chat 1");
        then(((ChatPaneController) emptyTab.getUserData()).messagesContainerChildren()).hasSize(1);
    }

    @Test
    void archive_button_calls_handler_and_closes_only_its_tab_on_success() throws Exception {
        GUIRenderer renderer = new GUIRenderer();
        Chat archivedChat = new Chat(new PromptMessage("A"));
        Chat remainingChat = new Chat(new PromptMessage("B"));
        Conversation conversation = new Conversation()
            .addChat(archivedChat)
            .addChat(remainingChat);
        AtomicReference<Chat> handledChat = new AtomicReference<>();
        renderer.archiveHandler(chat -> {
            handledChat.set(chat);
            return true;
        });

        interact(() -> renderer.render(conversation));
        WaitForAsyncUtils.waitForFxEvents();
        Tab archivedTab = renderer.getTabs().get(0);
        Tab remainingTab = renderer.getTabs().get(1);
        Button archiveButton = archiveButton(archivedTab);
        then(archiveButton(remainingTab).getAccessibleText()).isEqualTo("Close and archive");

        interact(archiveButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        then(archiveButton.getAccessibleText()).isEqualTo("Close and archive");
        then(handledChat.get()).isSameAs(archivedChat);
        then(renderer.getTabs()).containsExactly(remainingTab);
    }

    @Test
    void archive_button_logs_each_message_with_type_name_and_multiline_content() throws Exception {
        GUIRenderer renderer = new GUIRenderer();
        Chat chat = new Chat(new PromptMessage("first line\nsecond line"));
        chat.addMessage(new ToolInvocationMessage(
            new InvocationJSONObject("search", new JSONObject("{\"query\":\"terms\"}"))
        ));
        renderer.archiveHandler(ignored -> false);

        Logger logger = Logger.getLogger(ConversationPaneController.class.getName());
        Level previousLevel = logger.getLevel();
        List<LogRecord> records = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        logger.setLevel(Level.ALL);
        logger.addHandler(handler);

        try {
            interact(() -> renderer.render(new Conversation().addChat(chat)));
            WaitForAsyncUtils.waitForFxEvents();

            ChatPaneController chatPane = (ChatPaneController) renderer.getTabs().get(0).getUserData();
            MessagePane promptPane = (MessagePane) chatPane.messagesContainerChildren().get(0);
            ToolMessagePane toolPane = (ToolMessagePane) chatPane.messagesContainerChildren().get(1);
            promptPane.setExpanded(false);
            toolPane.setExpanded(true);

            interact(() -> archiveButton(renderer.getTabs().get(0)).fire());

            then(records).hasSize(1);
            then(records.get(0).getLevel()).isEqualTo(Level.FINE);
            then(records.get(0).getMessage())
                .contains("Type: PROMPT")
                .contains("Title/name: VIBER")
                .contains("Content:\nfirst line\nsecond line")
                .contains("Pane state: collapsed")
                .contains("Type: TOOL_EXECUTION_REQUEST")
                .contains("Title/name: search")
                .contains("Pane state: unfolded");
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(previousLevel);
        }
    }

    @Test
    void archive_button_keeps_tab_open_when_handler_does_not_archive() throws Exception {
        GUIRenderer renderer = new GUIRenderer();
        Chat chat = new Chat(new PromptMessage("A"));
        renderer.archiveHandler(ignored -> false);

        interact(() -> renderer.render(new Conversation().addChat(chat)));
        WaitForAsyncUtils.waitForFxEvents();
        Tab tab = renderer.getTabs().get(0);

        interact(archiveButton(tab)::fire);
        WaitForAsyncUtils.waitForFxEvents();

        then(renderer.getTabs()).containsExactly(tab);
    }

    @Test
    void tab_title_precedes_compact_archive_and_forget_buttons() throws Exception {
        GUIRenderer renderer = new GUIRenderer();
        renderer.archiveHandler(ignored -> false);
        renderer.forgetHandler(ignored -> false);

        interact(() -> renderer.render(new Conversation().addChat(new Chat(new PromptMessage("A")))));
        WaitForAsyncUtils.waitForFxEvents();

        Tab tab = renderer.getTabs().get(0);
        HBox header = (HBox) tab.getGraphic();
        Button archive = archiveButton(tab);
        Button forget = forgetButton(tab);

        then(tab.getText()).isNull();
        then(header.getChildren().get(0)).isInstanceOf(Label.class);
        then(((Label) header.getChildren().get(0)).getText()).isEqualTo("Chat 1");
        then(header.getChildren().get(1)).isInstanceOf(Separator.class);
        then(header.getChildren().get(2)).isSameAs(archive);
        then(header.getChildren().get(3)).isSameAs(forget);
        then(archive.getPrefWidth()).isEqualTo(18);
        then(forget.getPrefWidth()).isEqualTo(18);
    }

    @Test
    void forget_button_confirms_before_calling_handler_and_closing_its_tab() throws Exception {
        GUIRenderer renderer = new GUIRenderer();
        Chat forgottenChat = new Chat(new PromptMessage("A"));
        Chat remainingChat = new Chat(new PromptMessage("B"));
        Conversation conversation = new Conversation()
            .addChat(forgottenChat)
            .addChat(remainingChat);
        AtomicReference<Chat> handledChat = new AtomicReference<>();
        renderer.archiveHandler(ignored -> false);
        renderer.forgetHandler(chat -> {
            handledChat.set(chat);
            return true;
        });

        interact(() -> renderer.render(conversation));
        WaitForAsyncUtils.waitForFxEvents();
        Tab forgottenTab = renderer.getTabs().get(0);
        Tab remainingTab = renderer.getTabs().get(1);
        Button forgetButton = forgetButton(forgottenTab);
        then(forgetButton(remainingTab).getAccessibleText()).isEqualTo("Close and forget");

        interact(forgetButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        AtomicReference<DialogPane> confirmation = new AtomicReference<>();
        interact(() -> confirmation.set(dialogPaneWithContent("completely discarded")));
        then(confirmation.get()).isNotNull();
        then(handledChat.get()).isNull();
        then(renderer.getTabs()).containsExactly(forgottenTab, remainingTab);

        interact(() -> dialogButton(confirmation.get(), "Forget").fire());
        WaitForAsyncUtils.waitForFxEvents();

        then(handledChat.get()).isSameAs(forgottenChat);
        then(renderer.getTabs()).containsExactly(remainingTab);
    }

    @Test
    void declining_forget_confirmation_preserves_the_tab_and_skips_handler() throws Exception {
        GUIRenderer renderer = new GUIRenderer();
        Chat chat = new Chat(new PromptMessage("A"));
        AtomicReference<Chat> handledChat = new AtomicReference<>();
        renderer.archiveHandler(ignored -> false);
        renderer.forgetHandler(candidate -> {
            handledChat.set(candidate);
            return true;
        });

        interact(() -> renderer.render(new Conversation().addChat(chat)));
        WaitForAsyncUtils.waitForFxEvents();
        Tab tab = renderer.getTabs().get(0);

        interact(forgetButton(tab)::fire);
        WaitForAsyncUtils.waitForFxEvents();
        AtomicReference<DialogPane> confirmation = new AtomicReference<>();
        interact(() -> confirmation.set(dialogPaneWithContent("completely discarded")));
        interact(() -> ((Button) confirmation.get().lookupButton(ButtonType.CANCEL)).fire());
        WaitForAsyncUtils.waitForFxEvents();

        then(handledChat.get()).isNull();
        then(renderer.getTabs()).containsExactly(tab);
    }

    @Test
    void dismissing_forget_confirmation_preserves_the_tab_and_skips_handler() throws Exception {
        GUIRenderer renderer = new GUIRenderer();
        Chat chat = new Chat(new PromptMessage("A"));
        AtomicReference<Chat> handledChat = new AtomicReference<>();
        renderer.archiveHandler(ignored -> false);
        renderer.forgetHandler(candidate -> {
            handledChat.set(candidate);
            return true;
        });

        interact(() -> renderer.render(new Conversation().addChat(chat)));
        WaitForAsyncUtils.waitForFxEvents();
        Tab tab = renderer.getTabs().get(0);

        interact(forgetButton(tab)::fire);
        WaitForAsyncUtils.waitForFxEvents();
        AtomicReference<DialogPane> confirmation = new AtomicReference<>();
        interact(() -> confirmation.set(dialogPaneWithContent("completely discarded")));
        interact(() -> confirmation.get().getScene().getWindow().hide());
        WaitForAsyncUtils.waitForFxEvents();

        then(handledChat.get()).isNull();
        then(renderer.getTabs()).containsExactly(tab);
    }

    private Button archiveButton(Tab tab) {
        HBox actions = (HBox) tab.getGraphic();
        return (Button) actions.getChildren().stream()
            .filter(Button.class::isInstance)
            .map(Button.class::cast)
            .filter(button -> button.getStyleClass().contains("chat-tab-archive-button"))
            .findFirst()
            .orElseThrow();
    }

    private Button forgetButton(Tab tab) {
        HBox actions = (HBox) tab.getGraphic();
        return (Button) actions.getChildren().stream()
            .filter(Button.class::isInstance)
            .map(Button.class::cast)
            .filter(button -> button.getStyleClass().contains("chat-tab-forget-button"))
            .findFirst()
            .orElseThrow();
    }

    private DialogPane dialogPaneWithContent(String content) {
        return Window.getWindows().stream()
            .filter(Window::isShowing)
            .filter(window -> window.getScene() != null)
            .map(window -> window.getScene().getRoot().lookup(".dialog-pane"))
            .filter(DialogPane.class::isInstance)
            .map(DialogPane.class::cast)
            .filter(dialog -> dialog.getContentText().contains(content))
            .findFirst()
            .orElseThrow();
    }

    private Button dialogButton(DialogPane dialogPane, String text) {
        return dialogPane.getButtonTypes().stream()
            .filter(buttonType -> buttonType.getText().equals(text))
            .map(dialogPane::lookupButton)
            .map(Button.class::cast)
            .findFirst()
            .orElseThrow();
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
