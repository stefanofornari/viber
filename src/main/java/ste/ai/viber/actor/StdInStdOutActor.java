package ste.ai.viber.actor;

import ste.ai.viber.model.Chat;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.model.ReplyMessage;

public class StdInStdOutActor implements Actor {
    private final Conversation conversation;

    public StdInStdOutActor() {
        this.conversation = new Conversation();
    }

    @Override
    public void chat(Chat chat) {
        PromptMessage prompt = chat.prompt();
        String reply = "Echo: " + prompt.content();
        chat.addMessage(new ReplyMessage(reply));
    }

    @Override
    public Conversation conversation() {
        return conversation;
    }
}
