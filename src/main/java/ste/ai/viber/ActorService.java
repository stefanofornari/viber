package ste.ai.viber;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.Result;

public interface ActorService {
    Result<String> chat(@UserMessage String userMessage);
}
