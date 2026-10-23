package ste.ai.viber.tools;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import dev.langchain4j.agent.tool.Tool;
import javafx.application.Platform;
import ste.lazyjava.safe.Safe;
import ste.llm.tools.ToolPolicy;
import ste.llm.tools.ToolPolicy.Policy;
import ste.ai.viber.renderer.QuestionsPanelProvider;
import ste.uzzo.agent.tools.AskTools;

/**
 * Tools to ask questions to the user through a QuestionsPanel in the chat window.
 * Examples of invocations:
 *
 * <pre>
 * {"tool":"AskQuestion", "arguments": {"question":"What's your name?"}}
 * {"tool":"AskSingleChoice", "arguments": {"question": "Where do you live?", "options": ["Rome", "Paris", "Rest of the World"]}}
 * {"tool":"AskMultipleChoice", "arguments": {"question": "Where have you worked?", "options": ["Rome", "Paris", "London", "Other"]}}
 * {"tool":"AskConfirmation", "arguments": {"question":"Is this a java application?"}}
 * </pre>
 */
public class ViberGUIAskTools implements AskTools, Safe {

    private final QuestionsPanelProvider panelProvider;

    public ViberGUIAskTools(QuestionsPanelProvider panelProvider) {
        this.panelProvider = panelProvider;
    }

    @Override
    @Tool(name = "AskQuestion", value = "Ask the user an open-ended question and return their response")
    @ToolPolicy(Policy.INTERACTIVE)
    public String askQuestion(final String question) {
        requireNonNull(question, "question");

        CompletableFuture<String> future = panelProvider.askQuestion(question);
        return waitForAnswer(future).orElse("");
    }

    @Override
    @Tool(name = "AskSingleChoice", value = "Ask the user to select one option from a list and return their choice")
    @ToolPolicy(Policy.INTERACTIVE)
    public String askSingleChoice(final String question, final List<String> options) {
        requireNonNull(question, "question");
        requireNonNull(options, "options");

        if (options.isEmpty()) {
            return "";
        }

        CompletableFuture<String> future = panelProvider.askSingleChoice(question, options);
        return waitForAnswer(future).orElse(options.get(0));
    }

    @Override
    @Tool(name = "AskMultipleChoice", value = "Ask the user to select multiple options from a list and return selected items")
    @ToolPolicy(Policy.INTERACTIVE)
    public List<String> askMultipleChoice(final String question, final List<String> options) {
        requireNonNull(question, "question");
        requireNonNull(options, "options");

        if (options.isEmpty()) {
            return List.of();
        }

        CompletableFuture<List<String>> future = panelProvider.askMultipleChoice(question, options);
        return waitForAnswer(future).orElse(List.of());
    }

    @Override
    @Tool(name = "AskConfirmation", value = "Ask the user to confirm a decision (yes/no) and return their choice")
    @ToolPolicy(Policy.INTERACTIVE)
    public boolean askConfirmation(final String question) {
        requireNonNull(question, "question");

        CompletableFuture<Boolean> future = panelProvider.askConfirmation(question);
        return waitForAnswer(future).orElse(false);
    }

    /**
     * Waits for the CompletableFuture to complete without timeout.
     * Answers are delivered by a message from the user, so no timeout is needed.
     */
    private <T> Optional<T> waitForAnswer(CompletableFuture<T> future) {
        try {
            // If we're on FX thread, we can't block - but tool execution should be on a background thread
            if (Platform.isFxApplicationThread()) {
                throw new IllegalStateException("Cannot wait for user input on JavaFX Application Thread");
            }
            return Optional.ofNullable(future.get()); // No timeout - wait indefinitely
        } catch (Exception e) {
            // Interruption or cancellation
            return Optional.empty();
        }
    }
}