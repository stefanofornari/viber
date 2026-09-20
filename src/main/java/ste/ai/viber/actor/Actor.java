package ste.ai.viber.actor;

import ste.ai.viber.model.Chat;
import ste.ai.viber.model.ChatMessage;
import ste.ai.viber.model.Conversation;

import java.util.function.Consumer;

public interface Actor {
    void chat(Chat chat, Consumer<ChatMessage> onMessage);

    Conversation conversation();
}
