package ste.ai.viber.tools;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import dev.langchain4j.agent.tool.Tool;
import java.util.function.Consumer;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Node;
import ste.ai.viber.renderer.QuestionsPaneManager;
import ste.lazyjava.safe.Safe;
import ste.llm.tools.ToolPolicy;
import ste.llm.tools.ToolPolicy.Policy;
import ste.lazyjava.annotation.NonNull;
import ste.lazyjava.annotation.NonEmpty;
import ste.lazyjava.annotation.NonBlank;
import ste.llm.chat.model.ToolInvocationMessage;
import ste.uzzo.agent.tools.AskTools;
import ste.uzzo.agent.tools.NoAnswerException;

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

    private final Logger LOG = Logger.getLogger(getClass().getName());

    private final QuestionsPaneManager paneManager;

    public ViberGUIAskTools(@NonNull final Consumer<Node> addQuestion) {
        paneManager = new QuestionsPaneManager(addQuestion);
    }

    @Override
    @Tool(name = ASK_QUESTION, value = "Ask the user an open-ended question and return their response") // TODO: REMOVE it should be inherited
    @ToolPolicy(Policy.INTERACTIVE)
    public String askQuestion(@NonBlank final String question) {
        LOG.finest(() -> "AskQuestion %s".formatted(question));
        final QuestionJSONObject askInv = new QuestionJSONObject(ASK_QUESTION, question);
        final ToolInvocationMessage toolMsg = new ToolInvocationMessage(askInv);

        return waitForAnswer(paneManager.askQuestion(toolMsg));
    }

    @Override
    @Tool(name = ASK_SINGLE_CHOICE, value = "Ask the user to select one option from a list and return their choice")
    @ToolPolicy(Policy.INTERACTIVE)
    public String askSingleChoice(@NonBlank final String question, @NonEmpty final List<String> options) {
        LOG.finest(() -> "AskSingleChoice %s %s".formatted(question, String.valueOf(options)));
        final QuestionJSONObject askInv = new QuestionJSONObject(ASK_SINGLE_CHOICE, question, options);
        final ToolInvocationMessage toolMsg = new ToolInvocationMessage(askInv);
        return waitForAnswer(paneManager.askSingleChoice(toolMsg));
    }

    @Override
    @Tool(name = ASK_MULTIPLE_CHOICE, value = "Ask the user to select multiple options from a list and return selected items")
    @ToolPolicy(Policy.INTERACTIVE)
    public List<String> askMultipleChoice(@NonBlank final String question, @NonEmpty final List<String> options) {
        LOG.finest(() -> "AskMultipleChoice %s".formatted(question));
        final QuestionJSONObject askInv = new QuestionJSONObject(ASK_MULTIPLE_CHOICE, question, options);
        final ToolInvocationMessage toolMsg = new ToolInvocationMessage(askInv);
        return waitForAnswer(paneManager.askMultipleChoice(toolMsg));
    }

    @Override
    @Tool(name = ASK_CONFIRMATION, value = "Ask the user to confirm a decision (yes/no) and return their choice")
    @ToolPolicy(Policy.INTERACTIVE)
    public boolean askConfirmation(@NonBlank final String question) {
        LOG.finest(() -> "AskConfirmation %s".formatted(question));
        final QuestionJSONObject askInv = new QuestionJSONObject(ASK_CONFIRMATION, question);
        final ToolInvocationMessage toolMsg = new ToolInvocationMessage(askInv);
        return waitForAnswer(paneManager.askConfirmation(toolMsg));
    }

    /**
     * Waits for the CompletableFuture to complete without timeout.
     * Answers are delivered by a message from the user, so no timeout is needed.
     */
    private <T> T waitForAnswer(CompletableFuture<T> future) throws NoAnswerException {
        // If we're on FX thread, we can't block - but tool execution should be on a background thread
        if (Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Cannot wait for user input on JavaFX Application Thread");
        }

        try {
            return future.get();
        } catch (final Exception x) {
            throw new NoAnswerException();
        }
    }
}