package ste.ai.viber.renderer;

import org.junit.jupiter.api.Test;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.Message;
import ste.ai.viber.model.MessageType;
import ste.ai.viber.model.Role;
import ste.ai.viber.model.SystemMessage;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

class StringRendererTest {

    @Test
    void renders_conversation_with_one_chat_containing_two_messages() throws Exception {
        Chat chat = new Chat();
        chat.addMessage(new Message(Role.VIBER, MessageType.PROMPT, "Hello"));
        chat.addMessage(new Message(Role.ACTOR, MessageType.REPLY, "Hi there"));

        Conversation conversation = new Conversation().addChat(chat);
        String output = tapSystemOut(() -> new StringRenderer().render(conversation));

        then(output)
            .isEqualTo("--- Chat 1 ---\n" +
                "  VIBER/PROMPT> Hello\n" +
                "  ACTOR/REPLY: Hi there\n" +
                "\n");
    }

    @Test
    void renders_multiple_chats_separated() throws Exception {
        Chat chat1 = new Chat();
        chat1.addMessage(new Message(Role.VIBER, MessageType.PROMPT, "Feature A?"));

        Chat chat2 = new Chat();
        chat2.addMessage(new Message(Role.VIBER, MessageType.PROMPT, "Feature B?"));

        Conversation conversation = new Conversation()
            .addChat(chat1)
            .addChat(chat2);

        String output = tapSystemOut(() -> new StringRenderer().render(conversation));

        then(output)
            .isEqualTo("--- Chat 1 ---\n" +
                "  VIBER/PROMPT> Feature A?\n" +
                "\n" +
                "--- Chat 2 ---\n" +
                "  VIBER/PROMPT> Feature B?\n" +
                "\n");
    }

    @Test
    void renders_different_message_types_distinctly() throws Exception {
        Chat chat = new Chat();
        chat.addMessage(new Message(Role.VIBER, MessageType.PROMPT, "prompt text"));
        chat.addMessage(new Message(Role.ACTOR, MessageType.REPLY, "reply text"));
        chat.addMessage(new Message(Role.ACTOR, MessageType.THOUGHT, "thought text"));

        Conversation conversation = new Conversation().addChat(chat);
        String output = tapSystemOut(() -> new StringRenderer().render(conversation));

        then(output)
            .isEqualTo("--- Chat 1 ---\n" +
                "  VIBER/PROMPT> prompt text\n" +
                "  ACTOR/REPLY: reply text\n" +
                "  ACTOR/THOUGHT~ thought text\n" +
                "\n");
    }

    @Test
    void renders_system_message_before_chats() throws Exception {
        Chat chat = new Chat();
        chat.addMessage(new Message(Role.VIBER, MessageType.PROMPT, "Hello"));

        Conversation conversation = new Conversation()
            .systemMessage(new SystemMessage("You are a helpful assistant"))
            .addChat(chat);

        String output = tapSystemOut(() -> new StringRenderer().render(conversation));

        then(output)
            .isEqualTo("--- System ---\n" +
                "  You are a helpful assistant\n" +
                "--- Chat 1 ---\n" +
                "  VIBER/PROMPT> Hello\n" +
                "\n");
    }

    @Test
    void renders_conversation_without_system_message() throws Exception {
        Chat chat = new Chat();
        chat.addMessage(new Message(Role.VIBER, MessageType.PROMPT, "Hello"));

        Conversation conversation = new Conversation().addChat(chat);
        String output = tapSystemOut(() -> new StringRenderer().render(conversation));

        then(output)
            .isEqualTo("--- Chat 1 ---\n" +
                "  VIBER/PROMPT> Hello\n" +
                "\n");
    }

    @Test
    void renders_empty_conversation_gracefully() throws Exception {
        Conversation conversation = new Conversation();
        String output = tapSystemOut(() -> new StringRenderer().render(conversation));

        then(output).isEqualTo("(empty conversation)\n");
    }

    @Test
    void renders_tool_messages_with_distinct_prefix() throws Exception {
        Chat chat = new Chat();
        chat.addMessage(new Message(Role.VIBER, MessageType.TOOL_EXECUTION_REQUEST, "search(query=foo)"));
        chat.addMessage(new Message(Role.ACTOR, MessageType.TOOL_EXECUTION_RESPONSE, "result: 3 items"));

        Conversation conversation = new Conversation().addChat(chat);
        String output = tapSystemOut(() -> new StringRenderer().render(conversation));

        then(output)
            .isEqualTo("--- Chat 1 ---\n" +
                "  VIBER/TOOL_EXECUTION_REQUEST[TOOL] search(query=foo)\n" +
                "  ACTOR/TOOL_EXECUTION_RESPONSE[OUT] result: 3 items\n" +
                "\n");
    }

    @Test
    void renders_single_chat_incrementally() throws Exception {
        Chat chat = new Chat();
        chat.addMessage(new Message(Role.VIBER, MessageType.PROMPT, "Hello"));

        String output = tapSystemOut(() -> new StringRenderer().render(chat));

        then(output)
            .isEqualTo("--- Chat 1 ---\n" +
                "  VIBER/PROMPT> Hello\n" +
                "\n");
    }

    @Test
    void renders_single_message_incrementally() throws Exception {
        Message message = new Message(Role.ACTOR, MessageType.REPLY, "Hi there");
        String output = tapSystemOut(() -> new StringRenderer().render(message));

        then(output).isEqualTo("  ACTOR/REPLY: Hi there\n");
    }

    @Test
    void render_throws_on_null_conversation() throws Exception {
        StringRenderer renderer = new StringRenderer();
        thenThrownBy(() -> tapSystemOut(() -> renderer.render((Conversation) null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("conversation must not be null");
    }

    @Test
    void renderChat_throws_on_null_chat() throws Exception {
        StringRenderer renderer = new StringRenderer();
        thenThrownBy(() -> tapSystemOut(() -> renderer.render((Chat) null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("chat must not be null");
    }

    @Test
    void renderMessage_throws_on_null_message() throws Exception {
        StringRenderer renderer = new StringRenderer();
        thenThrownBy(() -> tapSystemOut(() -> renderer.render((Message) null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("message must not be null");
    }
}
