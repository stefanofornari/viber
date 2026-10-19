package ste.ai.viber.tools;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import dev.langchain4j.agent.tool.Tool;
import ste.llm.tools.ToolPolicy;
import ste.llm.tools.ToolPolicy.Policy;
import ste.uzzo.agent.tools.AskTools;

import java.lang.reflect.Method;

class ViberGUIAskToolsTest {

    @Test
    void askQuestion_returns_user_input() {
        TestableViberGUIAskTools tools = new TestableViberGUIAskTools();
        tools.questionResponse = Optional.of("My answer");

        String result = tools.askQuestion("What is your name?");

        then(result).isEqualTo("My answer");
    }

    @Test
    void askQuestion_returns_empty_string_when_cancelled() {
        TestableViberGUIAskTools tools = new TestableViberGUIAskTools();
        tools.questionResponse = Optional.empty();

        String result = tools.askQuestion("Question?");

        then(result).isEqualTo("");
    }

    @Test
    void askQuestion_throws_on_null_question() {
        TestableViberGUIAskTools tools = new TestableViberGUIAskTools();

        thenThrownBy(() -> tools.askQuestion(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("question must not be null");
    }

    @Test
    void askSingleChoice_returns_selected_option() {
        TestableViberGUIAskTools tools = new TestableViberGUIAskTools();
        tools.singleChoiceResponse = Optional.of("Mobile");

        String result = tools.askSingleChoice("Choose a platform", List.of("Web", "Mobile", "Desktop"));

        then(result).isEqualTo("Mobile");
    }

    @Test
    void askSingleChoice_returns_first_option_when_cancelled() {
        TestableViberGUIAskTools tools = new TestableViberGUIAskTools();
        tools.singleChoiceResponse = Optional.empty();

        String result = tools.askSingleChoice("Choose", List.of("Web", "Mobile"));

        then(result).isEqualTo("Web");
    }

    @Test
    void askSingleChoice_returns_empty_string_for_empty_options() {
        TestableViberGUIAskTools tools = new TestableViberGUIAskTools();

        String result = tools.askSingleChoice("Choose", List.of());

        then(result).isEqualTo("");
    }

    @Test
    void askSingleChoice_throws_on_null_question() {
        TestableViberGUIAskTools tools = new TestableViberGUIAskTools();

        thenThrownBy(() -> tools.askSingleChoice(null, List.of("A", "B")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("question must not be null");
    }

    @Test
    void askMultipleChoice_returns_selected_options() {
        TestableViberGUIAskTools tools = new TestableViberGUIAskTools();
        tools.multipleChoiceResponse = Optional.of(List.of("Web", "Desktop"));

        List<String> result = tools.askMultipleChoice("Choose platforms", List.of("Web", "Mobile", "Desktop"));

        then(result).containsExactly("Web", "Desktop");
    }

    @Test
    void askMultipleChoice_returns_empty_list_when_cancelled() {
        TestableViberGUIAskTools tools = new TestableViberGUIAskTools();
        tools.multipleChoiceResponse = Optional.empty();

        List<String> result = tools.askMultipleChoice("Choose", List.of("A", "B"));

        then(result).isEmpty();
    }

    @Test
    void askMultipleChoice_returns_empty_list_for_empty_options() {
        TestableViberGUIAskTools tools = new TestableViberGUIAskTools();

        List<String> result = tools.askMultipleChoice("Choose", List.of());

        then(result).isEmpty();
    }

    @Test
    void askConfirmation_returns_true_for_yes() {
        TestableViberGUIAskTools tools = new TestableViberGUIAskTools();
        tools.confirmationResponse = Optional.of(javafx.scene.control.ButtonType.YES);

        boolean result = tools.askConfirmation("Do you agree?");

        then(result).isTrue();
    }

    @Test
    void askConfirmation_returns_false_for_no() {
        TestableViberGUIAskTools tools = new TestableViberGUIAskTools();
        tools.confirmationResponse = Optional.of(javafx.scene.control.ButtonType.NO);

        boolean result = tools.askConfirmation("Do you agree?");

        then(result).isFalse();
    }

    @Test
    void askConfirmation_returns_false_when_cancelled() {
        TestableViberGUIAskTools tools = new TestableViberGUIAskTools();
        tools.confirmationResponse = Optional.empty();

        boolean result = tools.askConfirmation("Do you agree?");

        then(result).isFalse();
    }

    @Test
    void askConfirmation_throws_on_null_question() {
        TestableViberGUIAskTools tools = new TestableViberGUIAskTools();

        thenThrownBy(() -> tools.askConfirmation(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("question must not be null");
    }

    @Test
    void implements_askTools_interface() {
        ViberGUIAskTools tools = new ViberGUIAskTools();

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

    static class TestableViberGUIAskTools extends ViberGUIAskTools {

        Optional<String> questionResponse = Optional.empty();
        Optional<String> singleChoiceResponse = Optional.empty();
        Optional<List<String>> multipleChoiceResponse = Optional.empty();
        Optional<javafx.scene.control.ButtonType> confirmationResponse = Optional.empty();

        @Override
        protected Optional<String> askQuestionDialog(String question) {
            return questionResponse;
        }

        @Override
        protected Optional<String> askSingleChoiceDialog(String question, List<String> options) {
            return singleChoiceResponse;
        }

        @Override
        protected Optional<List<String>> askMultipleChoiceDialog(String question, List<String> options) {
            return multipleChoiceResponse;
        }

        @Override
        protected Optional<javafx.scene.control.ButtonType> askConfirmationDialog(String question) {
            return confirmationResponse;
        }
    }
}
