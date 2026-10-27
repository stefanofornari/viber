package ste.ai.viber.tools;

import static org.assertj.core.api.BDDAssertions.then;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import ste.uzzo.agent.tools.NoAnswerException;

class ViberGUIAskToolsTest extends ApplicationTest {

    private VBox root = null;

    final private Consumer<Node> ADD_QUESTION = (node) -> {
        Platform.runLater(() -> root.getChildren().add(node));
    };

    private final ViberGUIAskTools TOOLS = new ViberGUIAskTools(ADD_QUESTION);

    @Override
    public void start(Stage stage) {
        root = new VBox();

        stage.setScene(new Scene(root, 400, 200));
        stage.show();
    }

    @BeforeEach
    void before(TestInfo testInfo) {
        final String name = testInfo.getTestMethod().get().getName();
        System.out.println('\n' + name + '\n' + "-".repeat(name.length()));
    }

    @Test
    void askQuestion_returns_user_input() throws Exception {
        final CompletableFuture<String> answer = CompletableFuture.supplyAsync(() -> {
            return TOOLS.askQuestion("What is your name?"); // This blocks this specific background thread, not your main thread
        });

        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#textAreaInput").write("My answer");
        clickOn(".button:default");

        then(answer.join()).isEqualTo("My answer");
    }

    @Test
    void askQuestion_returns_empty_string_when_cancelled() throws Exception {
        final CompletableFuture<String> answer = CompletableFuture.supplyAsync(() -> {
            return TOOLS.askQuestion("What is your name?"); // This blocks this specific background thread, not your main thread
        });

        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#textAreaInput").write("My answer");
        clickOn("#cancelButton");

        thenThrownBy(() -> answer.join())
            .hasCauseInstanceOf(NoAnswerException.class).hasMessageEndingWith(NoAnswerException.DEFAULT_NO_ANSWER);
    }

    @Test
    void askQuestion_throws_on_null_or_empty_question() {
        thenThrownBy(() -> TOOLS.askQuestion(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("question must be non blank");

        thenThrownBy(() -> TOOLS.askQuestion(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("question must be non blank");

        thenThrownBy(() -> TOOLS.askQuestion(" \n"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("question must be non blank");
    }

    @Test
    void askSingleChoice_returns_selected_option() throws Exception {
        final CompletableFuture<String> answer = CompletableFuture.supplyAsync(() -> {
            return TOOLS.askSingleChoice("Choose a platform", List.of("Web", "Mobile", "Desktop"));
        });

        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#choiceBoxInput");
        clickOn("Mobile");
        clickOn(".button:default");

        then(answer.join()).isEqualTo("Mobile");
    }

    @Test
    void askSingleChoice_throws_on_null_or_empty_options() throws Exception {
        thenThrownBy(() -> TOOLS.askSingleChoice("pick your choice", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("options must be non empty");

        thenThrownBy(() -> TOOLS.askSingleChoice("pick your choice", List.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("options must be non empty");
    }

    @Test
    void askMultipleChoice_returns_selected_options() throws Exception {
        final CompletableFuture<List<String>> answer = CompletableFuture.supplyAsync(() -> {
            return TOOLS.askMultipleChoice("Choose platforms", List.of("Web", "Mobile", "Desktop"));
        });

        WaitForAsyncUtils.waitForFxEvents();

        Thread.sleep(200);

        clickOn("Web");
        clickOn("Desktop");
        clickOn(".button:default");

        then(answer.join()).containsOnly("Web", "Desktop");
    }

    @Test
    void askMultipleChoice_throws_on_null_or_empty_options() throws Exception {
        thenThrownBy(() -> TOOLS.askMultipleChoice("pick your choices", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("options must be non empty");

        thenThrownBy(() -> TOOLS.askMultipleChoice("pick your choices", List.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("options must be non empty");
    }

    @Test
    void askConfirmation_returns_yes() throws Exception {
        final CompletableFuture<Boolean> answer = CompletableFuture.supplyAsync(() -> {
            return TOOLS.askConfirmation("Do you agree?");
        });

        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#choiceBoxInput");
        clickOn("Yes");
        clickOn(".button:default");

        then(answer.join()).isTrue();
    }

    @Test
    void askConfirmation_returns_no() throws Exception {
        final CompletableFuture<Boolean> answer = CompletableFuture.supplyAsync(() -> {
            return TOOLS.askConfirmation("Do you agree?");
        });

        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#choiceBoxInput");
        clickOn("No");
        clickOn(".button:default");

        then(answer.join()).isFalse();
    }

    @Test
    void askConfirmation_throws_on_blank_question() {
        thenThrownBy(() -> TOOLS.askConfirmation(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("question must be non blank");

        thenThrownBy(() -> TOOLS.askConfirmation(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("question must be non blank");

        thenThrownBy(() -> TOOLS.askConfirmation(" \n"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("question must be non blank");
    }
}