package ste.ai.viber;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface ActorService {
    TokenStream chat(@UserMessage String userMessage);
}
