package ste.ai.viber.actor;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * Creates the streaming chat models used by the actors.
 */
public final class ChatModels {

    private ChatModels() {
    }

    /**
     * Creates an OpenAI-compatible streaming chat model.
     *
     * @param endpoint the base URL of the LLM provider
     * @param apiKey the API key
     * @param model the model name
     * @return the streaming chat model
     */
    public static StreamingChatModel openAi(String endpoint, String apiKey, String model) {
        return OpenAiStreamingChatModel.builder()
            .baseUrl(endpoint)
            .apiKey(apiKey)
            .modelName(model)
            .returnThinking(true)
            .logRequests(true)
            .logResponses(true)
            .build();
    }
}
