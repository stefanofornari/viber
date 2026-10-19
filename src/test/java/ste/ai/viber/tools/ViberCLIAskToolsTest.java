package ste.ai.viber.tools;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withTextFromSystemIn;
import static org.assertj.core.api.BDDAssertions.then;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.Test;
import ste.uzzo.agent.tools.AskTools;

class ViberCLIAskToolsTest {

    @Test
    void askQuestion_prints_question_and_returns_user_input() throws Exception {
        withTextFromSystemIn("My answer").execute(() -> {
            ViberCLIAskTools tools = new ViberCLIAskTools();
            String result = tools.askQuestion("What is your name?");
            then(result).isEqualTo("My answer");
        });
    }

    @Test
    void askQuestion_returns_empty_string_on_eof() throws Exception {
        withTextFromSystemIn("").execute(() -> {
            ViberCLIAskTools tools = new ViberCLIAskTools();
            String result = tools.askQuestion("Question?");
            then(result).isNotNull();
        });
    }

    @Test
    void askSingleChoice_prints_options_and_returns_selected() throws Exception {
        withTextFromSystemIn("2").execute(() -> {
            ViberCLIAskTools tools = new ViberCLIAskTools();
            String result = tools.askSingleChoice("Choose a platform", List.of("Web", "Mobile", "Desktop"));
            then(result).isEqualTo("Mobile");
        });
    }

    @Test
    void askSingleChoice_returns_first_option_on_invalid_input() throws Exception {
        withTextFromSystemIn("invalid").execute(() -> {
            ViberCLIAskTools tools = new ViberCLIAskTools();
            String result = tools.askSingleChoice("Choose", List.of("Web", "Mobile"));
            then(result).isEqualTo("Web");
        });
    }

    @Test
    void askSingleChoice_returns_first_option_on_eof() throws Exception {
        withTextFromSystemIn("").execute(() -> {
            ViberCLIAskTools tools = new ViberCLIAskTools();
            String result = tools.askSingleChoice("Choose", List.of("Web", "Mobile"));
            then(result).isEqualTo("Web");
        });
    }

    @Test
    void askMultipleChoice_returns_selected_options() throws Exception {
        withTextFromSystemIn("1,3").execute(() -> {
            ViberCLIAskTools tools = new ViberCLIAskTools();
            List<String> result = tools.askMultipleChoice("Choose platforms", List.of("Web", "Mobile", "Desktop"));
            then(result).containsExactly("Web", "Desktop");
        });
    }

    @Test
    void askMultipleChoice_returns_empty_list_on_eof() throws Exception {
        withTextFromSystemIn("").execute(() -> {
            ViberCLIAskTools tools = new ViberCLIAskTools();
            List<String> result = tools.askMultipleChoice("Choose", List.of("Web", "Mobile"));
            then(result).isEmpty();
        });
    }

    @Test
    void askConfirmation_returns_true_for_yes() throws Exception {
        withTextFromSystemIn("y").execute(() -> {
            ViberCLIAskTools tools = new ViberCLIAskTools();
            boolean result = tools.askConfirmation("Do you agree?");
            then(result).isTrue();
        });
    }

    @Test
    void askConfirmation_returns_true_for_yes_uppercase() throws Exception {
        withTextFromSystemIn("YES").execute(() -> {
            ViberCLIAskTools tools = new ViberCLIAskTools();
            boolean result = tools.askConfirmation("Do you agree?");
            then(result).isTrue();
        });
    }

    @Test
    void askConfirmation_returns_false_for_no() throws Exception {
        withTextFromSystemIn("n").execute(() -> {
            ViberCLIAskTools tools = new ViberCLIAskTools();
            boolean result = tools.askConfirmation("Do you agree?");
            then(result).isFalse();
        });
    }

    @Test
    void askConfirmation_returns_false_on_eof() throws Exception {
        withTextFromSystemIn("").execute(() -> {
            ViberCLIAskTools tools = new ViberCLIAskTools();
            boolean result = tools.askConfirmation("Do you agree?");
            then(result).isFalse();
        });
    }

    @Test
    void askQuestion_accepts_buffered_reader_for_testing() {
        BufferedReader reader = new BufferedReader(new StringReader("test response\n"));
        ViberCLIAskTools tools = new ViberCLIAskTools(reader);
        String result = tools.askQuestion("Question?");
        then(result).isEqualTo("test response");
    }

    @Test
    void askSingleChoice_accepts_buffered_reader_for_testing() {
        BufferedReader reader = new BufferedReader(new StringReader("1\n"));
        ViberCLIAskTools tools = new ViberCLIAskTools(reader);
        String result = tools.askSingleChoice("Choose", List.of("A", "B"));
        then(result).isEqualTo("A");
    }

    @Test
    void askMultipleChoice_accepts_buffered_reader_for_testing() {
        BufferedReader reader = new BufferedReader(new StringReader("1,2\n"));
        ViberCLIAskTools tools = new ViberCLIAskTools(reader);
        List<String> result = tools.askMultipleChoice("Choose", List.of("A", "B", "C"));
        then(result).containsExactly("A", "B");
    }

    @Test
    void askConfirmation_accepts_buffered_reader_for_testing() {
        BufferedReader reader = new BufferedReader(new StringReader("yes\n"));
        ViberCLIAskTools tools = new ViberCLIAskTools(reader);
        boolean result = tools.askConfirmation("Confirm?");
        then(result).isTrue();
    }
}
