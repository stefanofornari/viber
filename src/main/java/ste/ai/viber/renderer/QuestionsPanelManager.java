package ste.ai.viber.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javafx.application.Platform;


/**
 * Manages the QuestionsPanel lifecycle and coordinates between tool invocations
 * and the UI. Ensures questions are displayed in the chat and answers are
 * returned to waiting tool methods.
 *
 * Each question is answered individually with its own Answer button.
 * When a question is answered, its future completes immediately.
 */
public class QuestionsPanelManager implements QuestionsPanelProvider {

    private QuestionsPanel currentPanel;
    private final List<QuestionsPanel.Question> pendingQuestions = new ArrayList<>();
    private Consumer<QuestionsPanel> panelConsumer; // Called when panel should be added to chat

    public QuestionsPanelManager() {}

    /**
     * Sets the consumer that will be called when a new QuestionsPanel needs to be added to the chat.
     * Typically this is set by the ChatPaneController or GUIRenderer.
     */
    public void panelConsumer(Consumer<QuestionsPanel> consumer) {
        this.panelConsumer = consumer;
    }

    /**
     * Creates a new QuestionsPanel for the given tool name, adds it to the chat via the consumer,
     * and returns it. This should be called when the ToolInvocationMessage is first received.
     * If a panel already exists (from a previous tool invocation in the same batch), reuses it.
     */
    public QuestionsPanel createPanel(String toolName) {
        if (panelConsumer == null) {
            throw new IllegalStateException("Panel consumer not set. Call panelConsumer() first.");
        }

        // Reuse existing panel if it's still active (not completed)
        if (currentPanel != null && !currentPanel.isDisabled()) {
            return currentPanel;
        }

        currentPanel = new QuestionsPanel();
        currentPanel.setToolName(toolName);

        // Set up per-question answer handler
        currentPanel.setOnQuestionAnswered(answeredQuestion -> {
            // The individual question's future is completed in QuestionsPanel.handleQuestionAnswer()
            // Check if all questions are answered
            if (pendingQuestions.stream().allMatch(q -> q.future.isDone())) {
                pendingQuestions.clear();
                currentPanel = null;
            }
        });
        currentPanel.setOnCancel(() -> {
            // Complete all pending futures with null
            pendingQuestions.forEach(q -> q.future.complete(null));
            pendingQuestions.clear();
            currentPanel = null;
        });

        // Pass to consumer to add to chat
        panelConsumer.accept(currentPanel);
        return currentPanel;
    }

    /**
     * Adds an open-ended question to the current panel and returns a future that completes when user answers.
     */
    public CompletableFuture<String> askQuestion(String question) {
        return addQuestionToCurrentPanel(QuestionsPanel.QuestionType.OPEN_ENDED, question, null)
            .thenApply(obj -> (String) obj);
    }

    /**
     * Adds a single choice question to the current panel and returns a future that completes when user answers.
     */
    public CompletableFuture<String> askSingleChoice(String question, List<String> options) {
        return addQuestionToCurrentPanel(QuestionsPanel.QuestionType.SINGLE_CHOICE, question, options)
            .thenApply(obj -> (String) obj);
    }

    /**
     * Adds a multiple choice question to the current panel and returns a future that completes when user answers.
     */
    public CompletableFuture<List<String>> askMultipleChoice(String question, List<String> options) {
        return addQuestionToCurrentPanel(QuestionsPanel.QuestionType.MULTIPLE_CHOICE, question, options)
            .thenApply(obj -> (List<String>) obj);
    }

    /**
     * Adds a confirmation question to the current panel and returns a future that completes when user answers.
     */
    public CompletableFuture<Boolean> askConfirmation(String question) {
        return addQuestionToCurrentPanel(QuestionsPanel.QuestionType.CONFIRMATION, question, null)
            .thenApply(obj -> obj != null && (Boolean) obj);
    }

    private <T> CompletableFuture<T> addQuestionToCurrentPanel(QuestionsPanel.QuestionType type, String question, List<String> options) {
        QuestionsPanel.Question q = new QuestionsPanel.Question(type, question, options);
        pendingQuestions.add(q);

        if (currentPanel != null) {
            Platform.runLater(() -> addQuestionToPanel(q));
        } else {
            // Panel not created yet - will be added when createPanel is called
            // This shouldn't happen in normal flow but handle gracefully
        }

        return q.future.thenApply(obj -> (T) obj);
    }

    private void addQuestionToPanel(QuestionsPanel.Question question) {
        if (currentPanel != null) {
            switch (question.type) {
                case OPEN_ENDED -> currentPanel.addOpenEndedQuestion(question.question);
                case SINGLE_CHOICE -> currentPanel.addSingleChoiceQuestion(question.question, question.options);
                case MULTIPLE_CHOICE -> currentPanel.addMultipleChoiceQuestion(question.question, question.options);
                case CONFIRMATION -> currentPanel.addConfirmationQuestion(question.question);
            }
        }
    }

    /**
     * Clears any pending questions (e.g., when conversation is reset).
     */
    public void clear() {
        pendingQuestions.clear();
        if (currentPanel != null) {
            currentPanel.clear();
            currentPanel = null;
        }
    }
}