package ste.ai.viber.actor;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import ste.uzzo.agent.scope.UzzoScopeAgent;
import ste.uzzo.agent.tools.AskTools;

public class ScopeActor extends AgentActorBase {

    private final UzzoScopeAgent agent;

    public ScopeActor(final StreamingChatModel chatModel, final AskTools askTools) {
        super();
        requireNonNull(chatModel, "chatModel");
        requireNonNull(askTools, "askTools");

        agent = AiServices.builder(UzzoScopeAgent.class)
            .streamingChatModel(chatModel)
            .chatMemory(MessageWindowChatMemory.withMaxMessages(Integer.MAX_VALUE))
            .tools(askTools)
            .build();
    }

    @Override
    protected TokenStream createTokenStream(String userMessage) {
        return agent.scope(userMessage);
    }
}