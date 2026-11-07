package ste.ai.viber.actor;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import ste.llm.tools.FileSystemTools;
import ste.uzzo.agent.scope.UzzoScopeAgent;
import ste.uzzo.agent.tools.AskTools;

public class ScopeActor extends AgentActorBase {

    private final UzzoScopeAgent agent;
    private final FileSystemTools fileSystemTools;

    public ScopeActor(final StreamingChatModel chatModel, final AskTools askTools) {
        this(chatModel, askTools, defaultFileSystemTools());
    }

    public ScopeActor(final StreamingChatModel chatModel, final FileSystemTools fileSystemTools, final AskTools askTools) {
        this(chatModel, askTools, fileSystemTools);
    }

    public ScopeActor(final StreamingChatModel chatModel, final AskTools askTools, final FileSystemTools fileSystemTools) {
        super();
        requireNonNull(chatModel, "chatModel");
        requireNonNull(askTools, "askTools");
        requireNonNull(fileSystemTools, "fileSystemTools");

        this.fileSystemTools = fileSystemTools;

        agent = AiServices.builder(UzzoScopeAgent.class)
            .streamingChatModel(chatModel)
            .chatMemory(MessageWindowChatMemory.withMaxMessages(Integer.MAX_VALUE))
            .tools(askTools, fileSystemTools)
            .build();
    }

    public FileSystemTools fileSystemTools() {
        return fileSystemTools;
    }

    private static FileSystemTools defaultFileSystemTools() {
        try {
            return new FileSystemTools(System.getProperty("user.dir"));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize FileSystemTools", e);
        }
    }

    @Override
    protected TokenStream createTokenStream(String userMessage) {
        return agent.scope(userMessage);
    }
}