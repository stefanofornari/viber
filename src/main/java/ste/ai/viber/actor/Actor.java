package ste.ai.viber.actor;

import ste.ai.viber.model.Chat;
import ste.ai.viber.model.Conversation;

public interface Actor {
    void chat(Chat chat);

    Conversation conversation();
}
