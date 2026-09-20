package ste.ai.viber.cli;

import dev.langchain4j.exception.AuthenticationException;
import dev.langchain4j.exception.RateLimitException;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import ste.ai.viber.actor.Actor;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.ChatMessage;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.ErrorMessage;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.model.ReplyMessage;

import static org.assertj.core.api.BDDAssertions.then;

class ViberCLITest {

    @Test
    void chat_authentication_exception_is_wrapped_with_user_friendly_message() {
        Actor actor = new Actor() {
            @Override
            public void chat(Chat chat, java.util.function.Consumer<ChatMessage> onMessage) {
                throw new RuntimeException("Streaming chat failed", new AuthenticationException("PAID_MODEL_AUTH_REQUIRED: You need to sign in to use this model."));
            }

            @Override
            public Conversation conversation() {
                return new Conversation();
            }
        };

        Conversation conversation = new Conversation();
        ViberCLI cli = new ViberCLI(actor, conversation, new ste.ai.viber.renderer.StringRenderer(), new StringReader("hello\n"));
        cli.start();

        then(conversation.chats()).hasSize(1);
        Chat chat = conversation.chats().get(0);
        then(chat.messages()).hasSize(3);
        then(chat.messages().get(0)).isInstanceOf(PromptMessage.class);
        ErrorMessage errorMessage = (ErrorMessage) chat.messages().get(1);
        then(errorMessage.content()).contains("Authentication failed");
        then(errorMessage.content()).contains("PAID_MODEL_AUTH_REQUIRED");
        then(errorMessage.cause()).isInstanceOf(AuthenticationException.class);
        then(chat.messages().get(2)).isInstanceOf(ReplyMessage.class);
    }

    @Test
    void chat_rate_limit_exception_is_wrapped_with_user_friendly_message() {
        Actor actor = new Actor() {
            @Override
            public void chat(Chat chat, java.util.function.Consumer<ChatMessage> onMessage) {
                throw new RuntimeException("Streaming chat failed", new RateLimitException("Rate limit exceeded"));
            }

            @Override
            public Conversation conversation() {
                return new Conversation();
            }
        };

        Conversation conversation = new Conversation();
        ViberCLI cli = new ViberCLI(actor, conversation, new ste.ai.viber.renderer.StringRenderer(), new StringReader("hello\n"));
        cli.start();

        then(conversation.chats()).hasSize(1);
        Chat chat = conversation.chats().get(0);
        then(chat.messages()).hasSize(3);
        then(chat.messages().get(0)).isInstanceOf(PromptMessage.class);
        ErrorMessage errorMessage = (ErrorMessage) chat.messages().get(1);
        then(errorMessage.content()).contains("Rate limit exceeded");
        then(errorMessage.cause()).isInstanceOf(RateLimitException.class);
        then(chat.messages().get(2)).isInstanceOf(ReplyMessage.class);
    }
}
