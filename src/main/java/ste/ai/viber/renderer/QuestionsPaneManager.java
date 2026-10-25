package ste.ai.viber.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.Node;
import ste.lazyjava.annotation.NonNull;
import ste.llm.chat.model.ToolInvocationMessage;


/**
 * Manages the QuestionsPane lifecycle and coordinates between tool invocations
 * and the UI. Ensures questions are displayed in the chat and answers are
 * returned to waiting tool methods.
 *
 * Each question is answered individually with its own Answer button.
 * When a question is answered, its future completes immediately.
 */
public class QuestionsPaneManager {

    private QuestionsPane currentPane;
    private final List<QuestionsPane.Question> pendingQuestions = new ArrayList<>();
    private final Consumer<Node> addQuestion; // Called when panel should be added to chat

    public QuestionsPaneManager(final Consumer<Node> addQuestion) {
        this.addQuestion = addQuestion;
    }

    /**
     * Creates a new QuestionsPane for the given tool name, adds it to the chat via the consumer,
     * and returns it. This should be called when the ToolInvocationMessage is first received.
     * If a panel already exists (from a previous tool invocation in the same batch), reuses it.
     */
    public QuestionsPane paneForTool(@NonNull final String toolName) {
        // Reuse existing panel if it's still active (not completed)
        if (currentPane != null && !currentPane.isDisabled()) {
            return currentPane;
        }

        currentPane = new QuestionsPane();
        currentPane.setToolName(toolName);

        // Pass to consumer to add to chat (async on FX thread)
        addQuestion.accept(currentPane);

        return currentPane;
    }

    /**
     * Adds an open-ended question to the current panel and returns a future that completes when user answers.
     */
    public CompletableFuture<String> askQuestion(final ToolInvocationMessage question) {
        return addQuestionToCurrentPane(question).thenApply(obj -> (String) obj);
    }

    /**
     * Adds a single choice question to the current panel and returns a future that completes when user answers.
     */
    public CompletableFuture<String> askSingleChoice(final ToolInvocationMessage question) {
        return addQuestionToCurrentPane(question).thenApply(obj -> (String) obj);
    }

    /**
     * Adds a multiple choice question to the current panel and returns a future that completes when user answers.
     */
    public CompletableFuture<List<String>> askMultipleChoice(final ToolInvocationMessage question) {
        return addQuestionToCurrentPane(question).thenApply(obj -> (List<String>) obj);
    }

    /**
     * Adds a confirmation question to the current panel and returns a future that completes when user answers.
     */
    public CompletableFuture<Boolean> askConfirmation(final ToolInvocationMessage question) {
        return addQuestionToCurrentPane(question).thenApply(obj -> "Yes".equals(obj));
    }

    /**
     * Clears any pending questions (e.g., when conversation is reset).
     */
    public void clear() {
        pendingQuestions.clear();
        if (currentPane != null) {
            currentPane.clear();
            currentPane = null;
        }
    }

    // --------------------------------------------------------- private methods

    private <T> CompletableFuture<T> addQuestionToCurrentPane(
        @NonNull final ToolInvocationMessage question
    ) {
        QuestionsPane.Question q = new QuestionsPane.Question(question);
        pendingQuestions.add(q);

        Platform.runLater(() -> paneForTool(q.type.toString()).questions.add(q));

        return q.future.thenApply(obj -> (T) obj);
    }
}