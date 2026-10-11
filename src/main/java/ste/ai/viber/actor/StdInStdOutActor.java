package ste.ai.viber.actor;

import java.util.function.Consumer;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.ChatMessage;
import ste.llm.chat.model.Conversation;
import ste.llm.chat.model.PromptMessage;
import ste.llm.chat.model.ReplyMessage;

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
