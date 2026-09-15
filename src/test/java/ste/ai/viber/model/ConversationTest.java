package ste.ai.viber.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class ConversationTest {

    @Test
    void stores_and_retrieves_system_message() {
        Conversation conversation = new Conversation();
        SystemMessage systemMessage = new SystemMessage("You are a helpful assistant");

        conversation.systemMessage(systemMessage);

        then(conversation.systemMessage()).contains(systemMessage);
        then(conversation.systemMessage().get().content()).isEqualTo("You are a helpful assistant");
    }

    @Test
    void system_message_is_empty_by_default() {
        Conversation conversation = new Conversation();

        then(conversation.systemMessage()).isEmpty();
    }

    @Test
    void fluent_addChat_and_systemMessage() {
        Conversation conversation = new Conversation();
        Chat chat = new Chat(new PromptMessage("test"));

        conversation.addChat(chat).systemMessage(new SystemMessage("test"));

        then(conversation.chats()).containsExactly(chat);
        then(conversation.systemMessage()).isPresent();
    }
}
