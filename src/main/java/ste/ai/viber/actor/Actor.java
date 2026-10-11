package ste.ai.viber.actor;


import java.util.function.Consumer;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.ChatMessage;
import ste.llm.chat.model.Conversation;

public interface Actor {
    void chat(Chat chat, Consumer<ChatMessage> onMessage);

    Conversation conversation();
}
