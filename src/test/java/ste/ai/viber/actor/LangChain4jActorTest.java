package ste.ai.viber.actor;

import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialThinkingContext;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.chat.response.StreamingHandle;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.output.TokenUsage;
import org.junit.jupiter.api.Test;
import ste.ai.llm.DummyStreamingChatModel;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.ChatMessage;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.model.ReplyMessage;
import ste.ai.viber.model.ToolExecutionRequestMessage;
import ste.ai.viber.model.ToolExecutionResponseMessage;
import ste.ai.viber.model.ThoughtMessage;
import ste.ai.viber.tools.InputTool;
import ste.ai.viber.tools.FileSystemTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withTextFromSystemIn;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

class LangChain4jActorTest {

    @Test
    void chat_throws_on_null_chat() {
        DummyStreamingChatModel model = new DummyStreamingChatModel();
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "system"
        );

        thenThrownBy(() -> actor.chat((Chat) null, msg -> {}))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("chat must not be null");
    }

    @Test
    void chat_throws_on_null_onMessage() {
        DummyStreamingChatModel model = new DummyStreamingChatModel();
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "system"
        );

        Chat chat = new Chat(new PromptMessage("hello"));

        thenThrownBy(() -> actor.chat(chat, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("onMessage must not be null");
    }

    @Test
    void chat_with_filesystem_tools_creates_actor_successfully() throws Exception {
        DummyStreamingChatModel model = new DummyStreamingChatModel();
        java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("viber-test");
        FileSystemTools fsTools = new FileSystemTools(tempDir.toString());
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(fsTools),
            "system"
        );

        then(actor.conversation()).isNotNull();
        then(actor.conversation().systemMessage()).isPresent();
    }

    @Test
    void chat_does_not_add_chat_twice_to_conversation() {
        DummyStreamingChatModel model = new DummyStreamingChatModel();
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "system"
        );

        Chat chat = new Chat(new PromptMessage("hello"));

        actor.chat(chat, msg -> {});
        actor.chat(chat, msg -> {});

        then(actor.conversation().chats()).containsExactly(chat);
    }

    @Test
    void chat_uses_prompt_content_to_call_model() throws Exception {
        String name = "Alice";
        DummyStreamingChatModel model = new DummyStreamingChatModel();
        model.toolChoice = dev.langchain4j.model.chat.request.ToolChoice.AUTO;
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "use mock 'hello world.2.txt'\n" +
            "To greet the user, use the InputTool with the prompt 'What is your name?'. execute tool input with arguments: What's your name?"
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
        DummyStreamingChatModel model = new DummyStreamingChatModel();
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "system"
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
        DummyStreamingChatModel model = new DummyStreamingChatModel();
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "use mock 'hello world.txt'\nYou are a helpful assistant."
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
        DummyStreamingChatModel model = new DummyStreamingChatModel();
        model.toolChoice = dev.langchain4j.model.chat.request.ToolChoice.AUTO;
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "use mock 'hello world.2.txt'\n" +
            "To greet the user, use the InputTool with the prompt 'What is your name?'. execute tool input with arguments: What's your name?"
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

    @Test
    void chat_buffers_partial_text_until_newline() {
        ControlledStreamingChatModel model = new ControlledStreamingChatModel(
            List.of("Hello ", "world\n"),
            "Hello world"
        );
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "system"
        );

        Chat chat = new Chat(new PromptMessage("hi"));
        List<ChatMessage> emitted = new ArrayList<>();
        actor.chat(chat, emitted::add);

        then(emitted).hasSize(1);
        then(emitted.getFirst()).isInstanceOf(ReplyMessage.class);
        then(((ReplyMessage) emitted.getFirst()).content().trim()).isEqualTo("Hello world");
        then(chat.messages()).hasSize(2);
        then(chat.messages().getLast()).isInstanceOf(ReplyMessage.class);
        then(((ReplyMessage) chat.messages().getLast()).content().trim()).isEqualTo("Hello world");
    }

    @Test
    void chat_emits_multiple_messages_for_multiple_newlines_in_single_chunk() {
        ControlledStreamingChatModel model = new ControlledStreamingChatModel(
            List.of("line1\nline2\n"),
            "line1\nline2\n"
        );
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "system"
        );

        Chat chat = new Chat(new PromptMessage("hi"));
        List<ChatMessage> emitted = new ArrayList<>();
        actor.chat(chat, emitted::add);

        then(emitted).hasSize(2);
        then(emitted).allSatisfy(msg -> then(msg).isInstanceOf(ReplyMessage.class));
        then(((ReplyMessage) emitted.get(0)).content().trim()).isEqualTo("line1");
        then(((ReplyMessage) emitted.get(1)).content().trim()).isEqualTo("line2");
    }

    @Test
    void chat_flushes_remaining_text_on_complete_without_trailing_newline() {
        ControlledStreamingChatModel model = new ControlledStreamingChatModel(
            List.of("no newline here"),
            "no newline here"
        );
        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "system"
        );

        Chat chat = new Chat(new PromptMessage("hi"));
        List<ChatMessage> emitted = new ArrayList<>();
        actor.chat(chat, emitted::add);

        then(emitted).hasSize(1);
        then(emitted.getFirst()).isInstanceOf(ReplyMessage.class);
        then(((ReplyMessage) emitted.getFirst()).content().trim()).isEqualTo("no newline here");
    }

    @Test
    void chat_emits_buffered_text_then_remaining_after_newline() {
        ControlledStreamingChatModel model = new ControlledStreamingChatModel(
            List.of("first\n", "second"),
            "first\nsecond"
        );

        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "system"
        );

        Chat chat = new Chat(new PromptMessage("hi"));
        List<ChatMessage> emitted = new ArrayList<>();
        actor.chat(chat, emitted::add);

        then(emitted).hasSize(2);
        then(((ReplyMessage) emitted.get(0)).content().trim()).isEqualTo("first");
        then(((ReplyMessage) emitted.get(1)).content().trim()).isEqualTo("second");
    }

    @Test
    void chat_emits_thought_message_for_partial_thinking() {
        ControlledStreamingChatModel model = new ControlledStreamingChatModel(
            List.of("answer"),
            "answer",
            List.of("thinking step")
        );

        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "system"
        );

        Chat chat = new Chat(new PromptMessage("hi"));
        List<ChatMessage> emitted = new ArrayList<>();
        actor.chat(chat, emitted::add);

        then(emitted).anySatisfy(msg -> {
            then(msg).isInstanceOf(ThoughtMessage.class);
            then(((ThoughtMessage) msg).content().trim()).isEqualTo("thinking step");
        });
        then(emitted).anySatisfy(msg -> {
            then(msg).isInstanceOf(ReplyMessage.class);
            then(((ReplyMessage) msg).content().trim()).isEqualTo("answer");
        });
    }

    @Test
    void chat_buffers_thinking_until_newline() {
        ControlledStreamingChatModel model = new ControlledStreamingChatModel(
            List.of("answer"),
            "answer",
            List.of("partial ", "thought\n")
        );

        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "system"
        );

        Chat chat = new Chat(new PromptMessage("hi"));
        List<ChatMessage> emitted = new ArrayList<>();
        actor.chat(chat, emitted::add);

        then(emitted).anySatisfy(msg -> {
            then(msg).isInstanceOf(ThoughtMessage.class);
            then(((ThoughtMessage) msg).content().trim()).isEqualTo("partial thought");
        });
    }

    @Test
    void chat_flushes_remaining_thinking_on_complete_without_trailing_newline() {
        ControlledStreamingChatModel model = new ControlledStreamingChatModel(
            List.of("answer"),
            "answer",
            List.of("no newline thinking")
        );

        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "system"
        );

        Chat chat = new Chat(new PromptMessage("hi"));
        List<ChatMessage> emitted = new ArrayList<>();
        actor.chat(chat, emitted::add);

        then(emitted).anySatisfy(msg -> {
            then(msg).isInstanceOf(ThoughtMessage.class);
            then(((ThoughtMessage) msg).content().trim()).isEqualTo("no newline thinking");
        });
    }

    private static class ControlledStreamingChatModel implements StreamingChatModel {
        private final List<String> partials;
        private final String finalText;
        private final List<String> thinkingParts;
        private final List<ChatModelListener> listeners = new ArrayList<>();

        ControlledStreamingChatModel(List<String> partials, String finalText) {
            this(partials, finalText, List.of());
        }

        ControlledStreamingChatModel(List<String> partials, String finalText, List<String> thinkingParts) {
            this.partials = partials;
            this.finalText = finalText;
            this.thinkingParts = thinkingParts;
        }

        @Override
        public List<ChatModelListener> listeners() {
            return Collections.unmodifiableList(listeners);
        }

        public void addListener(ChatModelListener listener) {
            listeners.add(listener);
        }

        @Override
        public void chat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
            for (String part : partials) {
                handler.onPartialResponse(
                    new PartialResponse(part),
                    new PartialResponseContext(new NoOpStreamingHandle())
                );
            }
            for (String thinkingPart : thinkingParts) {
                handler.onPartialThinking(
                    new PartialThinking(thinkingPart),
                    new PartialThinkingContext(new NoOpStreamingHandle())
                );
            }
            handler.onCompleteResponse(
                ChatResponse.builder()
                    .aiMessage(AiMessage.from(finalText))
                    .build()
            );
        }

        @Override
        public Set<Capability> supportedCapabilities() {
            return StreamingChatModel.super.supportedCapabilities();
        }

        @Override
        public ChatRequestParameters defaultRequestParameters() {
            return StreamingChatModel.super.defaultRequestParameters();
        }

        @Override
        public ModelProvider provider() {
            return StreamingChatModel.super.provider();
        }
    }

    private static class NoOpStreamingHandle implements StreamingHandle {
        @Override public void cancel() {}
        @Override public boolean isCancelled() { return false; }
    }

    @Test
    void chat_stores_token_usage_on_chat_when_model_returns_it() {
        DummyStreamingChatModel model = new DummyStreamingChatModel();
        model.tokenUsage = new TokenUsage(10, 20, 30);

        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "system"
        );

        Chat chat = new Chat(new PromptMessage("hi"));
        List<ChatMessage> emitted = new ArrayList<>();
        actor.chat(chat, emitted::add);

        then(chat.tokenUsage()).isNotNull();
        then(chat.tokenUsage().inputTokenCount()).isEqualTo(10);
        then(chat.tokenUsage().outputTokenCount()).isEqualTo(20);
        then(chat.tokenUsage().totalTokenCount()).isEqualTo(30);
    }

    @Test
    void chat_leaves_token_usage_null_when_model_does_not_return_it() {
        DummyStreamingChatModel model = new DummyStreamingChatModel();

        LangChain4jActor actor = new LangChain4jActor(
            model,
            List.of(new InputTool()),
            "system"
        );

        Chat chat = new Chat(new PromptMessage("hi"));
        List<ChatMessage> emitted = new ArrayList<>();
        actor.chat(chat, emitted::add);

        then(chat.tokenUsage()).isNull();
    }
}
