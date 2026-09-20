package ste.ai.viber;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.junit.jupiter.api.Test;
import ste.ai.model.DummyChatModel;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.ChatMessage;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.model.ReplyMessage;
import ste.ai.viber.model.ToolExecutionRequestMessage;
import ste.ai.viber.model.ToolExecutionResponseMessage;
import ste.ai.viber.tools.InputTool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withTextFromSystemIn;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

class LangChain4jActorTest {

    @Test
    void chat_throws_on_null_chat() {
        DummyChatModel model = new DummyChatModel();
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "system",
            null
        );

        thenThrownBy(() -> actor.chat((Chat) null, msg -> {}))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("chat must not be null");
    }

    @Test
    void chat_throws_on_null_onMessage() {
        DummyChatModel model = new DummyChatModel();
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "system",
            null
        );

        Chat chat = new Chat(new PromptMessage("hello"));

        thenThrownBy(() -> actor.chat(chat, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("onMessage must not be null");
    }

    @Test
    void chat_does_not_add_chat_twice_to_conversation() {
        DummyChatModel model = new DummyChatModel();
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "system",
            null
        );

        Chat chat = new Chat(new PromptMessage("hello"));

        actor.chat(chat, msg -> {});
        actor.chat(chat, msg -> {});

        then(actor.conversation().chats()).containsExactly(chat);
    }

    @Test
    void chat_uses_prompt_content_to_call_model() throws Exception {
        String name = "Alice";
        DummyChatModel model = new DummyChatModel();
        model.toolChoice = dev.langchain4j.model.chat.request.ToolChoice.AUTO;
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "use mock 'hello world.2.txt'\n" +
            "To greet the user, use the InputTool with the prompt 'What is your name?'. execute tool input with arguments: What's your name?",
            null
        );

        Chat chat = new Chat(new PromptMessage("greet me"));

        List<ChatMessage> emitted = new ArrayList<>();
        withTextFromSystemIn(name).execute(() -> {
            actor.chat(chat, emitted::add);
        });

        then(emitted).anySatisfy(msg -> {
            then(msg).isInstanceOf(ReplyMessage.class);
            then(((ReplyMessage) msg).content().trim()).isEqualTo("Hello Alice!");
        });
    }

    @Test
    void chat_appends_messages_to_existing_chat() {
        DummyChatModel model = new DummyChatModel();
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "system",
            null
        );

        Chat chat = new Chat(new PromptMessage("hello"));
        List<ChatMessage> emitted = new ArrayList<>();
        actor.chat(chat, emitted::add);

        then(chat.messages()).hasSizeGreaterThanOrEqualTo(2);
        then(chat.messages().getFirst()).isInstanceOf(PromptMessage.class);
        then(chat.messages().getLast()).isInstanceOf(ReplyMessage.class);
    }

    @Test
    void chat_emits_multiple_reply_messages_for_streamed_chunks() {
        DummyChatModel model = new DummyChatModel();
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "use mock 'hello world.txt'\nYou are a helpful assistant.",
            null
        );

        Chat chat = new Chat(new PromptMessage("say hello"));
        List<ChatMessage> emitted = new ArrayList<>();
        actor.chat(chat, emitted::add);

        then(emitted).anySatisfy(msg -> {
            then(msg).isInstanceOf(ReplyMessage.class);
            then(((ReplyMessage) msg).content().trim()).isEqualTo("hello world");
        });
    }

    @Test
    void chat_emits_tool_messages_through_callback() throws Exception {
        DummyChatModel model = new DummyChatModel();
        model.toolChoice = dev.langchain4j.model.chat.request.ToolChoice.AUTO;
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "use mock 'hello world.2.txt'\n" +
            "To greet the user, use the InputTool with the prompt 'What is your name?'. execute tool input with arguments: What's your name?",
            null
        );

        Chat chat = new Chat(new PromptMessage("greet me"));
        List<ChatMessage> emitted = new ArrayList<>();

        withTextFromSystemIn("Alice").execute(() -> {
            actor.chat(chat, emitted::add);
        });

        then(emitted).anySatisfy(msg -> {
            then(msg).isInstanceOf(ToolExecutionRequestMessage.class);
        });
        then(emitted).anySatisfy(msg -> {
            then(msg).isInstanceOf(ToolExecutionResponseMessage.class);
        });
        then(emitted).anySatisfy(msg -> {
            then(msg).isInstanceOf(ReplyMessage.class);
            then(((ReplyMessage) msg).content().trim()).isEqualTo("Hello Alice!");
        });
    }
}
