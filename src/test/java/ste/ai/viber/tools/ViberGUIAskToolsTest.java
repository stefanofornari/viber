package ste.ai.viber.tools;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import dev.langchain4j.agent.tool.Tool;
import ste.llm.tools.ToolPolicy;
import ste.llm.tools.ToolPolicy.Policy;
import ste.ai.viber.renderer.QuestionsPanelProvider;
import ste.uzzo.agent.tools.AskTools;

import java.lang.reflect.Method;
import java.util.Collections;

class ViberGUIAskToolsTest {

    final private QuestionsPanelProvider questionsPanelManager = new QuestionsPanelProvider() {
    @Override
    public CompletableFuture<String> askQuestion(String question) {
        return CompletableFuture.completedFuture("Answer for: " + question);
    }

    @Override
    public CompletableFuture<String> askSingleChoice(String question, List<String> options) {
        return CompletableFuture.completedFuture(
            (options != null && !options.isEmpty()) ? options.get(0) : "Default Choice"
        );
    }

    @Override
    public CompletableFuture<List<String>> askMultipleChoice(String question, List<String> options) {
        return CompletableFuture.completedFuture(
            (options != null && !options.isEmpty())
            ? Collections.singletonList(options.get(0))
            : Collections.emptyList()
        );
    }

    @Override
    public CompletableFuture<Boolean> askConfirmation(String question) {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public void clear() { }
};

    @Test
    void askQuestion_returns_user_input() throws Exception {
        QuestionsPanelProvider mockProvider = new MockQuestionsPanelProvider()
            .withQuestionResponse("My answer");

        ViberGUIAskTools tools = new ViberGUIAskTools(mockProvider);

        String result = tools.askQuestion("What is your name?");

        then(result).isEqualTo("My answer");
    }

    @Test
    void askQuestion_returns_empty_string_when_cancelled() throws Exception {
        QuestionsPanelProvider mockProvider = new MockQuestionsPanelProvider()
            .withQuestionResponse(null);

        ViberGUIAskTools tools = new ViberGUIAskTools(mockProvider);

        String result = tools.askQuestion("Question?");

        then(result).isEqualTo("");
    }

    @Test
    void askQuestion_throws_on_null_question() {
        ViberGUIAskTools tools = new ViberGUIAskTools(new MockQuestionsPanelProvider());

        thenThrownBy(() -> tools.askQuestion(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("question must not be null");
    }

    @Test
    void askSingleChoice_returns_selected_option() throws Exception {
        QuestionsPanelProvider mockProvider = new MockQuestionsPanelProvider()
            .withSingleChoiceResponse("Mobile");

        ViberGUIAskTools tools = new ViberGUIAskTools(mockProvider);

        String result = tools.askSingleChoice("Choose a platform", List.of("Web", "Mobile", "Desktop"));

        then(result).isEqualTo("Mobile");
    }

    @Test
    void askSingleChoice_returns_first_option_when_cancelled() throws Exception {
        QuestionsPanelProvider mockProvider = new MockQuestionsPanelProvider()
            .withSingleChoiceResponse(null);

        ViberGUIAskTools tools = new ViberGUIAskTools(mockProvider);

        String result = tools.askSingleChoice("Choose", List.of("Web", "Mobile"));

        then(result).isEqualTo("Web");
    }

    @Test
    void askSingleChoice_returns_empty_string_for_empty_options() throws Exception {
        ViberGUIAskTools tools = new ViberGUIAskTools(new MockQuestionsPanelProvider());

        String result = tools.askSingleChoice("Choose", List.of());

        then(result).isEqualTo("");
    }

    @Test
    void askSingleChoice_throws_on_null_question() {
        ViberGUIAskTools tools = new ViberGUIAskTools(new MockQuestionsPanelProvider());

        thenThrownBy(() -> tools.askSingleChoice(null, List.of("A", "B")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("question must not be null");
    }

    @Test
    void askMultipleChoice_returns_selected_options() throws Exception {
        QuestionsPanelProvider mockProvider = new MockQuestionsPanelProvider()
            .withMultipleChoiceResponse(List.of("Web", "Desktop"));

        ViberGUIAskTools tools = new ViberGUIAskTools(mockProvider);

        List<String> result = tools.askMultipleChoice("Choose platforms", List.of("Web", "Mobile", "Desktop"));

        then(result).containsExactly("Web", "Desktop");
    }

    @Test
    void askMultipleChoice_returns_empty_list_when_cancelled() throws Exception {
        QuestionsPanelProvider mockProvider = new MockQuestionsPanelProvider()
            .withMultipleChoiceResponse(null);

        ViberGUIAskTools tools = new ViberGUIAskTools(mockProvider);

        List<String> result = tools.askMultipleChoice("Choose", List.of("A", "B"));

        then(result).isEmpty();
    }

    @Test
    void askMultipleChoice_returns_empty_list_for_empty_options() throws Exception {
        ViberGUIAskTools tools = new ViberGUIAskTools(new MockQuestionsPanelProvider());

        List<String> result = tools.askMultipleChoice("Choose", List.of());

        then(result).isEmpty();
    }

    @Test
    void askConfirmation_returns_true_for_yes() throws Exception {
        QuestionsPanelProvider mockProvider = new MockQuestionsPanelProvider()
            .withConfirmationResponse(true);

        ViberGUIAskTools tools = new ViberGUIAskTools(mockProvider);

        boolean result = tools.askConfirmation("Do you agree?");

        then(result).isTrue();
    }

    @Test
    void askConfirmation_returns_false_for_no() throws Exception {
        QuestionsPanelProvider mockProvider = new MockQuestionsPanelProvider()
            .withConfirmationResponse(false);

        ViberGUIAskTools tools = new ViberGUIAskTools(mockProvider);

        boolean result = tools.askConfirmation("Do you agree?");

        then(result).isFalse();
    }

    @Test
    void askConfirmation_returns_false_when_cancelled() throws Exception {
        QuestionsPanelProvider mockProvider = new MockQuestionsPanelProvider()
            .withConfirmationResponse(null);

        ViberGUIAskTools tools = new ViberGUIAskTools(mockProvider);

        boolean result = tools.askConfirmation("Do you agree?");

        then(result).isFalse();
    }

    @Test
    void askConfirmation_throws_on_null_question() {
        ViberGUIAskTools tools = new ViberGUIAskTools(new MockQuestionsPanelProvider());

        thenThrownBy(() -> tools.askConfirmation(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("question must not be null");
    }

    @Test
    void implements_askTools_interface() {
        ViberGUIAskTools tools = new ViberGUIAskTools(questionsPanelManager);

        then(tools).isInstanceOf(AskTools.class);
    }

    @Test
    void askQuestion_method_has_tool_annotation() throws Exception {
        Method method = ViberGUIAskTools.class.getMethod("askQuestion", String.class);
        then(method.isAnnotationPresent(Tool.class)).isTrue();
        Tool tool = method.getAnnotation(Tool.class);
        then(tool.name()).isEqualTo("AskQuestion");
    }

    @Test
    void askSingleChoice_method_has_tool_annotation() throws Exception {
        Method method = ViberGUIAskTools.class.getMethod("askSingleChoice", String.class, List.class);
        then(method.isAnnotationPresent(Tool.class)).isTrue();
        Tool tool = method.getAnnotation(Tool.class);
        then(tool.name()).isEqualTo("AskSingleChoice");
    }

    @Test
    void askMultipleChoice_method_has_tool_annotation() throws Exception {
        Method method = ViberGUIAskTools.class.getMethod("askMultipleChoice", String.class, List.class);
        then(method.isAnnotationPresent(Tool.class)).isTrue();
        Tool tool = method.getAnnotation(Tool.class);
        then(tool.name()).isEqualTo("AskMultipleChoice");
    }

    @Test
    void askConfirmation_method_has_tool_annotation() throws Exception {
        Method method = ViberGUIAskTools.class.getMethod("askConfirmation", String.class);
        then(method.isAnnotationPresent(Tool.class)).isTrue();
        Tool tool = method.getAnnotation(Tool.class);
        then(tool.name()).isEqualTo("AskConfirmation");
    }

    @Test
    void all_askTools_methods_have_interactive_tool_policy() throws Exception {
        Method[] methods = ViberGUIAskTools.class.getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(Tool.class)) {
                then(method.isAnnotationPresent(ToolPolicy.class))
                    .as(method.getName()).isTrue();
                ToolPolicy policy = method.getAnnotation(ToolPolicy.class);
                then(policy.value()).isEqualTo(Policy.INTERACTIVE);
            }
        }
    }

    /**
     * Mock implementation of QuestionsPanelProvider for testing.
     * Returns pre-configured CompletableFutures for each question type.
     */
    static class MockQuestionsPanelProvider implements QuestionsPanelProvider {

        private String questionResponse;
        private String singleChoiceResponse;
        private List<String> multipleChoiceResponse;
        private Boolean confirmationResponse;

        MockQuestionsPanelProvider withQuestionResponse(String response) {
            this.questionResponse = response;
            return this;
        }

        MockQuestionsPanelProvider withSingleChoiceResponse(String response) {
            this.singleChoiceResponse = response;
            return this;
        }

        MockQuestionsPanelProvider withMultipleChoiceResponse(List<String> response) {
            this.multipleChoiceResponse = response;
            return this;
        }

        MockQuestionsPanelProvider withConfirmationResponse(Boolean response) {
            this.confirmationResponse = response;
            return this;
        }

        @Override
        public CompletableFuture<String> askQuestion(String question) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.complete(questionResponse);
            return future;
        }

        @Override
        public CompletableFuture<String> askSingleChoice(String question, List<String> options) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.complete(singleChoiceResponse);
            return future;
        }

        @Override
        public CompletableFuture<List<String>> askMultipleChoice(String question, List<String> options) {
            CompletableFuture<List<String>> future = new CompletableFuture<>();
            future.complete(multipleChoiceResponse);
            return future;
        }

        @Override
        public CompletableFuture<Boolean> askConfirmation(String question) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.complete(confirmationResponse);
            return future;
        }

        @Override
        public void clear() {
            // No-op for testing
        }
    }
}