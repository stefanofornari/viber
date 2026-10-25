package ste.ai.viber.renderer;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import ste.llm.chat.model.ToolInvocationMessage;

/**
 * Interface for managing questions panel interactions.
 * Allows for dependency injection and testing.
 */
public interface QuestionsPaneProvider {

    CompletableFuture<String> askQuestion(ToolInvocationMessage question);

    CompletableFuture<String> askSingleChoice(ToolInvocationMessage question);

    CompletableFuture<List<String>> askMultipleChoice(ToolInvocationMessage question);

    CompletableFuture<Boolean> askConfirmation(ToolInvocationMessage question);
}