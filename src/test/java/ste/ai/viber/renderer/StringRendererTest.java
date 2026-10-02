package ste.ai.viber.renderer;

import org.junit.jupiter.api.Test;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.ChatMessage;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.model.ReplyMessage;
import ste.ai.viber.model.Role;
import ste.ai.viber.model.ThoughtMessage;
import ste.ai.viber.model.ToolInvocationMessage;
import ste.ai.viber.model.ToolExecutionMessage;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

class StringRendererTest {

    @Test
    void renders_conversation_with_one_chat_containing_two_messages() throws Exception {
        Chat chat = new Chat(new PromptMessage("Hello"));
        chat.addMessage(new ReplyMessage("Hi there"));

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
        Chat chat1 = new Chat(new PromptMessage("Feature A?"));
        Chat chat2 = new Chat(new PromptMessage("Feature B?"));

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
        Chat chat = new Chat(new PromptMessage("prompt text"));
        chat.addMessage(new ReplyMessage("reply text"));
        chat.addMessage(new ThoughtMessage("thought text"));

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
        Chat chat = new Chat(new PromptMessage("Hello"));

        Conversation conversation = new Conversation()
            .systemMessage(new ste.ai.viber.model.SystemMessage("You are a helpful assistant"))
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
        Chat chat = new Chat(new PromptMessage("Hello"));

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
        Chat chat = new Chat(new PromptMessage("Hello"));
        chat.addMessage(new ToolInvocationMessage("search(query=foo)"));
        chat.addMessage(new ToolExecutionMessage("result: 3 items"));

        Conversation conversation = new Conversation().addChat(chat);
        String output = tapSystemOut(() -> new StringRenderer().render(conversation));

        then(output)
            .isEqualTo("--- Chat 1 ---\n" +
                "  VIBER/PROMPT> Hello\n" +
                "  ACTOR/TOOL_EXECUTION_REQUEST[TOOL] search(query=foo)\n" +
                "  VIBER/TOOL_EXECUTION_RESPONSE[OUT] result: 3 items\n" +
                "\n");
    }

    @Test
    void renders_single_chat_incrementally() throws Exception {
        Chat chat = new Chat(new PromptMessage("Hello"));

        String output = tapSystemOut(() -> new StringRenderer().render(chat));

        then(output)
            .isEqualTo("--- Chat 1 ---\n" +
                "  VIBER/PROMPT> Hello\n" +
                "\n");
    }

    @Test
    void renders_single_message_incrementally() throws Exception {
        ChatMessage message = new ReplyMessage("Hi there");
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
        thenThrownBy(() -> tapSystemOut(() -> renderer.render((ChatMessage) null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("message must not be null");
    }
}
