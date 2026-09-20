package ste.ai.viber.actor;

import ste.ai.viber.model.Chat;
import ste.ai.viber.model.ChatMessage;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.model.ReplyMessage;

import java.util.function.Consumer;

public class StdInStdOutActor implements Actor {
    private final Conversation conversation;

    public StdInStdOutActor() {
        this.conversation = new Conversation();
    }

    @Override
    public void chat(Chat chat, Consumer<ChatMessage> onMessage) {
        PromptMessage prompt = chat.prompt();
        String reply = "Echo: " + prompt.content();
        ReplyMessage replyMessage = new ReplyMessage(reply);
        chat.addMessage(replyMessage);
        onMessage.accept(replyMessage);
    }

    @Override
    public Conversation conversation() {
        return conversation;
    }
}
