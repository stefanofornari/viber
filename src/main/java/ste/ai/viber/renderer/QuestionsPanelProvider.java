package ste.ai.viber.renderer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for managing questions panel interactions.
 * Allows for dependency injection and testing.
 */
public interface QuestionsPanelProvider {

    CompletableFuture<String> askQuestion(String question);

    CompletableFuture<String> askSingleChoice(String question, List<String> options);

    CompletableFuture<List<String>> askMultipleChoice(String question, List<String> options);

    CompletableFuture<Boolean> askConfirmation(String question);

    void clear();
}