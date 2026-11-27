package ste.ai.viber.actor;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import java.util.List;
import ste.lazyjava.annotation.NonBlank;
import ste.llm.chat.model.SystemMessage;
import ste.llm.tools.Tool;

public class LangChain4jActor extends AgentActorBase {
    public interface ActorService {
        TokenStream chat(@UserMessage String userMessage);
    }

    private final ActorService service;

    public LangChain4jActor(
        StreamingChatModel streamingChatModel,
        List<Tool> tools,
        @NonBlank String systemPrompt
    ) {
        requireNonNull(streamingChatModel, "streamingChatModel");
        requireFullyNonNull(tools, "tools");

        conversation.systemMessage(new SystemMessage(systemPrompt));

        this.service = AiServices.builder(ActorService.class)
            .streamingChatModel(streamingChatModel)
            .tools(tools.toArray())
            .systemMessageProvider(o -> systemPrompt)
            .build();
    }

    @Override
    protected TokenStream createTokenStream(String userMessage) {
        return service.chat(userMessage);
    }
}
