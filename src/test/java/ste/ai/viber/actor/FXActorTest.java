package ste.ai.viber.actor;

import org.junit.jupiter.api.Test;
import ste.ai.viber.renderer.Renderer;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.json.JSONObject;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.Conversation;
import ste.llm.chat.model.ErrorMessage;
import ste.llm.chat.model.PromptMessage;
import ste.llm.chat.model.ReplyMessage;
import ste.llm.chat.model.ThoughtMessage;
import ste.llm.chat.model.ToolExecutionMessage;
import ste.llm.chat.model.ToolInvocationMessage;
import ste.xtest.json.api.JSONAssertions;

class FXActorTest {

    @Test
    void chat_adds_chat_to_conversation_and_sets_current() {
        Conversation conversation = new Conversation();
        RecordingRenderer renderer = new RecordingRenderer();
        FXActor actor = new FXActor(conversation, renderer);

        Chat chat = new Chat(new PromptMessage("hello"));
        actor.chat(chat, msg -> {});

        then(conversation.chats()).contains(chat);
        then(actor.currentChat()).isEqualTo(chat);
    }

    @Test
    void chat_renders_conversation() {
        Conversation conversation = new Conversation();
        RecordingRenderer renderer = new RecordingRenderer();
        FXActor actor = new FXActor(conversation, renderer);

        Chat chat = new Chat(new PromptMessage("hello"));
        actor.chat(chat, msg -> {});

        then(renderer.renderedConversations).contains(conversation);
    }

    @Test
    void addReply_appends_reply_to_current_chat() {
        Conversation conversation = new Conversation();
        RecordingRenderer renderer = new RecordingRenderer();
        FXActor actor = new FXActor(conversation, renderer);

        Chat chat = new Chat(new PromptMessage("hello"));
        actor.chat(chat, msg -> {});
        actor.addReply("world");

        then(chat.messages()).hasSize(2);
        then(chat.messages().getLast()).isInstanceOf(ReplyMessage.class);
        then(chat.messages().getLast().content()).isEqualTo("world");
        then(renderer.renderedMessages).contains(chat.messages().getLast());
    }

    @Test
    void addThought_appends_thought_to_current_chat() {
        Conversation conversation = new Conversation();
        RecordingRenderer renderer = new RecordingRenderer();
        FXActor actor = new FXActor(conversation, renderer);

        Chat chat = new Chat(new PromptMessage("hello"));
        actor.chat(chat, msg -> {});
        actor.addThought("thinking");

        then(chat.messages()).hasSize(2);
        then(chat.messages().getLast()).isInstanceOf(ThoughtMessage.class);
        then(chat.messages().getLast().content()).isEqualTo("thinking");
        then(renderer.renderedMessages).contains(chat.messages().getLast());
    }

    @Test
    void addToolExecution_appends_tool_request_to_current_chat() {
        Conversation conversation = new Conversation();
        RecordingRenderer renderer = new RecordingRenderer();
        FXActor actor = new FXActor(conversation, renderer);

        Chat chat = new Chat(new PromptMessage("hello"));
        actor.chat(chat, msg -> {});
        actor.addToolInvocation("{\"arguments\":{\"args\":\"value\"},\"tool\":\"manual\"}");

        then(chat.messages()).hasSize(2);
        then(chat.messages().getLast()).isInstanceOf(ToolInvocationMessage.class);
        JSONAssertions.then(
            new JSONObject(chat.messages().getLast().content())
        ).isEqualTo(
            new JSONObject("{\"tool\":\"manual\",\"arguments\":{\"args\":\"value\"}}")
        );
    }

    @Test
    void addToolReply_appends_tool_response_to_current_chat() {
        Conversation conversation = new Conversation();
        RecordingRenderer renderer = new RecordingRenderer();
        FXActor actor = new FXActor(conversation, renderer);

        Chat chat = new Chat(new PromptMessage("hello"));
        actor.chat(chat, msg -> {});
        actor.addToolReply("result");

        then(chat.messages()).hasSize(2);
        then(chat.messages().getLast()).isInstanceOf(ToolExecutionMessage.class);
        then(chat.messages().getLast().content()).isEqualTo("result");
    }

    @Test
    void addError_appends_error_to_current_chat() {
        Conversation conversation = new Conversation();
        RecordingRenderer renderer = new RecordingRenderer();
        FXActor actor = new FXActor(conversation, renderer);

        Chat chat = new Chat(new PromptMessage("hello"));
        actor.chat(chat, msg -> {});
        actor.addError("something went wrong");

        then(chat.messages()).hasSize(2);
        then(chat.messages().getLast()).isInstanceOf(ErrorMessage.class);
        then(chat.messages().getLast().content()).isEqualTo("something went wrong");
    }

    @Test
    void addMessage_throws_when_no_current_chat() {
        Conversation conversation = new Conversation();
        RecordingRenderer renderer = new RecordingRenderer();
        FXActor actor = new FXActor(conversation, renderer);

        thenThrownBy(() -> actor.addReply("text"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No active chat");
    }

    @Test
    void conversation_returns_shared_conversation() {
        Conversation conversation = new Conversation();
        RecordingRenderer renderer = new RecordingRenderer();
        FXActor actor = new FXActor(conversation, renderer);

        then(actor.conversation()).isSameAs(conversation);
    }

    private static class RecordingRenderer implements Renderer {
        List<Conversation> renderedConversations = new ArrayList<>();
        List<ste.llm.chat.model.ChatMessage> renderedMessages = new ArrayList<>();

        @Override
        public void render(Conversation conversation) {
            renderedConversations.add(conversation);
        }

        @Override
        public void render(Chat chat) {
        }

        @Override
        public void render(ste.llm.chat.model.ChatMessage message) {
            renderedMessages.add(message);
        }
    }
}