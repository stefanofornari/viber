package ste.ai.viber.cli;

import dev.langchain4j.model.chat.request.ToolChoice;
import org.junit.jupiter.api.Test;
import ste.ai.model.DummyChatModel;
import ste.ai.viber.actor.Actor;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.model.ReplyMessage;
import ste.ai.viber.LangChain4jActor;
import ste.ai.viber.tools.InputTool;

import java.util.List;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withTextFromSystemIn;
import static org.assertj.core.api.BDDAssertions.then;

class VibeChatCLITest {

    @Test
    void starts_session_and_creates_conversation_with_initial_prompt() throws Exception {
        DummyChatModel model = new DummyChatModel();
        model.toolChoice = ToolChoice.AUTO;

        Actor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "use mock cli_hello.txt\nYou are a helpful assistant.",
            null
        );

        String[] output = new String[1];
        withTextFromSystemIn("hello", "/exit").execute(() -> {
            output[0] = tapSystemOut(() -> {
                VibeChatCLI cli = new VibeChatCLI(actor);
                cli.start();
            });
        });

        then(actor.conversation().chats()).hasSize(1);
        then(actor.conversation().chats().get(0).messages()).hasSize(2);
        then(actor.conversation().chats().get(0).messages().get(0))
            .isInstanceOf(PromptMessage.class)
            .extracting("content").isEqualTo("hello");
        then(actor.conversation().chats().get(0).messages().get(1))
            .isInstanceOf(ReplyMessage.class)
            .extracting("content").isEqualTo("Hello! How can I help you today?");
        then(output[0]).contains("Hello! How can I help you today?");
    }

    @Test
    void continues_existing_conversation_and_appends_new_chat() throws Exception {
        DummyChatModel model = new DummyChatModel();
        model.toolChoice = ToolChoice.AUTO;

        Actor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "use mock cli_hello.txt\nYou are a helpful assistant.",
            null
        );

        Chat existingChat = new Chat(new PromptMessage("previous question"));
        existingChat.addMessage(new ReplyMessage("previous answer"));
        actor.conversation().addChat(existingChat);

        String[] output = new String[1];
        withTextFromSystemIn("follow up", "/exit").execute(() -> {
            output[0] = tapSystemOut(() -> {
                VibeChatCLI cli = new VibeChatCLI(actor);
                cli.start();
            });
        });

        then(actor.conversation().chats()).hasSize(2);
        then(actor.conversation().chats().get(0).messages()).hasSize(2);
        then(actor.conversation().chats().get(1).messages()).hasSize(2);
        then(actor.conversation().chats().get(1).messages().get(0))
            .isInstanceOf(PromptMessage.class)
            .extracting("content").isEqualTo("follow up");
        then(actor.conversation().chats().get(1).messages().get(1))
            .isInstanceOf(ReplyMessage.class)
            .extracting("content").isEqualTo("Hello! How can I help you today?");
        then(output[0]).contains("previous answer");
        then(output[0]).contains("Hello! How can I help you today?");
    }

    @Test
    void exits_gracefully_on_exit_command() throws Exception {
        DummyChatModel model = new DummyChatModel();
        model.toolChoice = ToolChoice.AUTO;

        Actor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "use mock cli_hello.txt\nYou are a helpful assistant.",
            null
        );

        String[] output = new String[1];
        withTextFromSystemIn("/exit").execute(() -> {
            output[0] = tapSystemOut(() -> {
                VibeChatCLI cli = new VibeChatCLI(actor);
                cli.start();
            });
        });

        then(actor.conversation().chats()).isEmpty();
        then(output[0]).contains("(empty conversation)");
    }
}
