package ste.ai.viber.actor;

import dev.langchain4j.model.chat.request.ToolChoice;
import org.junit.jupiter.api.Test;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.ChatMessage;
import ste.llm.chat.model.PromptMessage;
import ste.llm.chat.model.ReplyMessage;
import ste.uzzo.agent.tools.AskTools;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import ste.xtest.llm.DummyStreamingChatModel;

class ScopeActorTest {

    @Test
    void constructor_throws_on_null_chatModel() {
        AskTools tools = new DummyAskTools();

        thenThrownBy(() -> new ScopeActor(null, tools))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("chatModel must not be null");
    }

    @Test
    void constructor_throws_on_null_askTools() {
        DummyStreamingChatModel model = new DummyStreamingChatModel();

        thenThrownBy(() -> new ScopeActor(model, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("askTools must not be null");
    }

    @Test
    void conversation_returns_non_null_conversation() {
        ScopeActor actor = new ScopeActor(new DummyStreamingChatModel(), new DummyAskTools());

        then(actor.conversation()).isNotNull();
    }

    @Test
    void chat_throws_on_null_chat() {
        ScopeActor actor = new ScopeActor(new DummyStreamingChatModel(), new DummyAskTools());

        thenThrownBy(() -> actor.chat((Chat) null, msg -> {}))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("chat must not be null");
    }

    @Test
    void chat_throws_on_null_onMessage() {
        ScopeActor actor = new ScopeActor(new DummyStreamingChatModel(), new DummyAskTools());
        Chat chat = new Chat(new PromptMessage("hello"));

        thenThrownBy(() -> actor.chat(chat, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("onMessage must not be null");
    }

    @Test
    void chat_does_not_add_chat_twice_to_conversation() {
        ScopeActor actor = new ScopeActor(new DummyStreamingChatModel(), new DummyAskTools());
        Chat chat = new Chat(new PromptMessage("use mock 'scope-response.txt'"));

        actor.chat(chat, msg -> {});
        actor.chat(chat, msg -> {});

        then(actor.conversation().chats()).containsExactly(chat);
    }

    @Test
    void chat_emits_reply_messages_split_by_newlines() {
        DummyStreamingChatModel model = new DummyStreamingChatModel();
        model.toolChoice = ToolChoice.NONE;
        ScopeActor actor = new ScopeActor(model, new DummyAskTools());

        Chat chat = new Chat(new PromptMessage("use mock 'scope response.txt'"));
        List<ChatMessage> emitted = new ArrayList<>();
        actor.chat(chat, emitted::add);

        then(emitted).allSatisfy(msg -> then(msg).isInstanceOf(ReplyMessage.class));
        then(((ReplyMessage) emitted.get(0)).content().trim()).isEqualTo("Phase 1: ClarificationWho are the primary users of this product?What features are essential vs nice-to-have?");
    }

    @Test
    void chat_appends_messages_to_existing_chat() {
        ScopeActor actor = new ScopeActor(new DummyStreamingChatModel(), new DummyAskTools());

        Chat chat = new Chat(new PromptMessage("use mock 'scope-response.txt'"));
        List<ChatMessage> emitted = new ArrayList<>();
        actor.chat(chat, emitted::add);

        then(chat.messages()).hasSizeGreaterThanOrEqualTo(2);
        then(chat.messages().getFirst()).isInstanceOf(PromptMessage.class);
        then(chat.messages().getLast()).isInstanceOf(ReplyMessage.class);
    }

    @Test
    void chat_adds_new_chat_to_conversation() {
        ScopeActor actor = new ScopeActor(new DummyStreamingChatModel(), new DummyAskTools());

        Chat chat = new Chat(new PromptMessage("use mock 'scope-response.txt'"));
        actor.chat(chat, msg -> {});

        then(actor.conversation().chats()).containsExactly(chat);
    }

    static class DummyAskTools implements AskTools {

        @Override
        public String askQuestion(String question) {
            return "A todo app for personal task management";
        }

        @Override
        public String askSingleChoice(String question, List<String> options) {
            return options.isEmpty() ? null : options.get(0);
        }

        @Override
        public List<String> askMultipleChoice(String question, List<String> options) {
            return options.isEmpty() ? List.of() : List.of(options.get(0));
        }

        @Override
        public boolean askConfirmation(String question) {
            return true;
        }
    }
}
