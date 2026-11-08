package ste.ai.viber.actor;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.ToolChoice;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.chat.response.StreamingHandle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.ChatMessage;
import ste.llm.chat.model.PromptMessage;
import ste.llm.chat.model.ReplyMessage;
import ste.llm.chat.model.ToolInvocationMessage;
import ste.llm.tools.FileSystemTools;
import ste.llm.tools.ToolPolicy;
import ste.xtest.llm.DummyStreamingChatModel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

class CodingActorTest {

    @Test
    void constructor_throws_on_null_chat_model() {
        thenThrownBy(() -> new CodingActor(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("chatModel must not be null");
    }

    @Test
    void constructor_throws_on_null_file_system_tools() throws Exception {
        thenThrownBy(() -> new CodingActor(new DummyStreamingChatModel(), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("fileSystemTools must not be null");
    }

    @Test
    void conversation_returns_non_null_conversation() {
        then(new CodingActor(new DummyStreamingChatModel()).conversation()).isNotNull();
    }

    @Test
    void chat_emits_agent_reply_through_actor_contract() {
        DummyStreamingChatModel model = new DummyStreamingChatModel();
        model.toolChoice = ToolChoice.NONE;
        CodingActor actor = new CodingActor(model);
        Chat chat = new Chat(new PromptMessage("use mock 'hello world.txt'"));
        List<ChatMessage> emitted = new ArrayList<>();

        actor.chat(chat, emitted::add);

        then(emitted).isNotEmpty().allSatisfy(message -> then(message).isInstanceOf(ReplyMessage.class));
        then(chat.messages().getLast()).isInstanceOf(ReplyMessage.class);
        then(actor.conversation().chats()).containsExactly(chat);
    }

    @Test
    void chat_exposes_file_system_tools_to_code_agent(@TempDir Path tempDir) throws Exception {
        DummyStreamingChatModel model = new DummyStreamingChatModel();
        model.toolChoice = ToolChoice.REQUIRED;
        CodingActor actor = new CodingActor(model, new FileSystemTools(tempDir.toString()));
        Chat chat = new Chat(new PromptMessage("execute tool ReadFile"));
        List<ChatMessage> emitted = new ArrayList<>();

        actor.chat(chat, emitted::add);

        then(emitted).anySatisfy(message -> then(message).isInstanceOf(ToolInvocationMessage.class));
    }

    @Test
    void code_agent_file_system_tools_have_behavior_appropriate_policies() {
        thenPolicy("readFile", ToolPolicy.Policy.READONLY);
        thenPolicy("findFiles", ToolPolicy.Policy.READONLY);
        thenPolicy("searchInFile", ToolPolicy.Policy.READONLY);
        thenPolicy("listFilesInDirectory", ToolPolicy.Policy.READONLY);
        thenPolicy("createFile", ToolPolicy.Policy.READWRITE);
        thenPolicy("deleteFile", ToolPolicy.Policy.READWRITE);
        thenPolicy("createDirectory", ToolPolicy.Policy.READWRITE);
        thenPolicy("deleteDirectory", ToolPolicy.Policy.READWRITE);
    }

    @Test
    void chat_can_continue_after_agent_failure() {
        CodingActor actor = new CodingActor(new FailingOnceStreamingChatModel());
        Chat failedChat = new Chat(new PromptMessage("first request"));
        Chat recoveredChat = new Chat(new PromptMessage("second request"));

        thenThrownBy(() -> actor.chat(failedChat, message -> {}))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Streaming chat failed");

        List<ChatMessage> emitted = new ArrayList<>();
        actor.chat(recoveredChat, emitted::add);

        then(emitted).hasSize(2).allSatisfy(message -> then(message).isInstanceOf(ReplyMessage.class));
        then(((ReplyMessage) emitted.get(0)).content()).isEqualTo("recovered first");
        then(((ReplyMessage) emitted.get(1)).content()).isEqualTo("recovered second");
        then(actor.conversation().chats()).containsExactly(failedChat, recoveredChat);
    }

    private static void thenPolicy(String methodName, ToolPolicy.Policy expectedPolicy) {
        try {
            ToolPolicy policy = FileSystemTools.class
                .getMethod(methodName, parameterTypesFor(methodName))
                .getAnnotation(ToolPolicy.class);
            then(policy).as(methodName).isNotNull();
            then(policy.value()).as(methodName).isEqualTo(expectedPolicy);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    private static Class<?>[] parameterTypesFor(String methodName) {
        return switch (methodName) {
            case "findFiles" -> new Class<?>[]{String.class, String.class};
            case "searchInFile" -> new Class<?>[]{String.class, String.class};
            case "createFile" -> new Class<?>[]{String.class, String.class};
            default -> new Class<?>[]{String.class};
        };
    }

    private static final class FailingOnceStreamingChatModel implements StreamingChatModel {
        private int requestCount;

        @Override
        public List<ChatModelListener> listeners() {
            return List.of();
        }

        @Override
        public void chat(ChatRequest request, StreamingChatResponseHandler handler) {
            if (requestCount++ == 0) {
                handler.onError(new IllegalStateException("simulated model failure"));
                return;
            }

            handler.onPartialResponse(new PartialResponse("recovered first\n"),
                new PartialResponseContext(new NoOpStreamingHandle()));
            handler.onPartialResponse(new PartialResponse("recovered second\n"),
                new PartialResponseContext(new NoOpStreamingHandle()));
            handler.onCompleteResponse(
                ChatResponse.builder().aiMessage(AiMessage.from("recovered first\nrecovered second\n")).build()
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

    private static final class NoOpStreamingHandle implements StreamingHandle {
        @Override
        public void cancel() {}

        @Override
        public boolean isCancelled() {
            return false;
        }
    }
}
