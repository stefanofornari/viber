package ste.ai.viber.actor;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.TokenStream;
import ste.llm.tools.FileSystemTools;
import ste.uzzo.agent.UzzoAgentBuilder;
import ste.uzzo.agent.code.UzzoCodeAgent;

/**
 * Viber actor that delegates coding requests to the Uzzo code agent.
 */
public class CodingActor extends AgentActorBase {

    private final UzzoCodeAgent agent;
    private final FileSystemTools fileSystemTools;

    /**
     * Creates a coding actor with file tools rooted at the current working directory.
     *
     * @param chatModel the streaming chat model used by the coding agent
     */
    public CodingActor(final StreamingChatModel chatModel) {
        this(chatModel, defaultFileSystemTools());
    }

    /**
     * Creates a coding actor with the supplied file tools.
     *
     * @param chatModel the streaming chat model used by the coding agent
     * @param fileSystemTools the tools exposed to the coding agent
     */
    public CodingActor(final StreamingChatModel chatModel, final FileSystemTools fileSystemTools) {
        super();
        requireNonNull(chatModel, "chatModel");
        requireNonNull(fileSystemTools, "fileSystemTools");

        this.fileSystemTools = fileSystemTools;

        agent = UzzoAgentBuilder.CodeAgentBuilder()
            .streamingChatModel(chatModel)
            .chatMemory(MessageWindowChatMemory.withMaxMessages(Integer.MAX_VALUE))
            .tools(fileSystemTools)
            .build();
    }

    /**
     * Returns the file tools configured for the coding agent.
     *
     * @return the configured file tools
     */
    public FileSystemTools fileSystemTools() {
        return fileSystemTools;
    }

    /**
     * Creates file tools rooted at the current working directory.
     *
     * @return file tools for the current working directory
     */
    private static FileSystemTools defaultFileSystemTools() {
        try {
            return new FileSystemTools(System.getProperty("user.dir"));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize FileSystemTools", e);
        }
    }

    /**
     * Delegates a user message to the Uzzo code agent.
     *
     * @param userMessage the message from the user
     * @return the code agent's response stream
     */
    @Override
    protected TokenStream createTokenStream(String userMessage) {
        return agent.implement(userMessage);
    }
}
