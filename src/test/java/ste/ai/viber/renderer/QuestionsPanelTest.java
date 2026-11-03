package ste.ai.viber.renderer;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.assertj.core.api.BDDAssertions.then;

import javafx.scene.control.TitledPane;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import ste.llm.chat.model.ToolInvocationMessage;
import ste.llm.tools.InvocationJSONObject;

class QuestionsPanelTest extends ApplicationTest {

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.show();
    }

    private final QuestionsPane.Question OPEN_ENDED = new QuestionsPane.Question(
        new ToolInvocationMessage(new InvocationJSONObject("AskQuestion", new JSONObject().put("question", "What is your name?")))
    );
    private final QuestionsPane.Question SINGLE_CHOICE = new QuestionsPane.Question(
        new ToolInvocationMessage(
            new InvocationJSONObject(
                "AskSingleChoice",
                new JSONObject(Map.of("question", "Choose platform", "options", List.of("Web", "Mobile", "Desktop")))
            )
        )
    );
    private final QuestionsPane.Question MULTIPLE_CHOICE = new QuestionsPane.Question(
        new ToolInvocationMessage(
            new InvocationJSONObject(
                "AskMultipleChoice",
                new JSONObject(Map.of("question", "Choose platforms", "options", List.of("Web", "Mobile", "Desktop")))
            )
        )
    );
    private final QuestionsPane.Question CONFIRMATION = new QuestionsPane.Question(
        new ToolInvocationMessage(new InvocationJSONObject("AskConfirmation", new JSONObject().put("question", "Do you agree?")))
    );

    @Test
    void questions_panel_is_titled_pane() {
        QuestionsPane panel = new QuestionsPane();
        then(panel).isInstanceOf(TitledPane.class);
    }

    @Test
    void questions_panel_has_correct_title_format() {
        QuestionsPane panel = new QuestionsPane();
        panel.setToolName("AskQuestion");
        then(panel.getText()).isEqualTo("ACTOR / AskQuestion");
    }

    @Test
    void questions_panel_can_add_open_ended_question() {
        QuestionsPane panel = new QuestionsPane();
        panel.questions.add(OPEN_ENDED);
        then(panel).isNotNull(); // TODO: NO SENSE
    }

    @Test
    void questions_panel_can_add_single_choice_question() {
        QuestionsPane panel = new QuestionsPane();
        panel.questions.add(SINGLE_CHOICE);
        then(panel).isNotNull(); // TODO: NO SENSE
    }

    @Test
    void questions_panel_can_add_multiple_choice_question() {
        QuestionsPane panel = new QuestionsPane();
        panel.questions.add(MULTIPLE_CHOICE);
        then(panel).isNotNull(); // TODO: NO SENSE
    }

    @Test
    void questions_panel_can_add_confirmation_question() {
        QuestionsPane panel = new QuestionsPane();
        panel.questions.add(CONFIRMATION);
        then(panel).isNotNull(); // TODO: NO SENSE
    }

    @Test
    void questions_panel_can_add_multiple_questions() {
        QuestionsPane panel = new QuestionsPane();
        panel.questions.add(OPEN_ENDED);
        panel.questions.add(SINGLE_CHOICE);
        panel.questions.add(CONFIRMATION);
        then(panel).isNotNull(); // TODO: NO SENSE
    }

    @Test
    void questions_panel_grows_to_content_and_caps_height() {
        QuestionsPane panel = new QuestionsPane();
        ScrollPane[] scrollPane = new ScrollPane[1];
        VBox[] content = new VBox[1];

        interact(() -> {
            for (int i = 0; i < 10; i++) {
                panel.questions.add(question("Question " + i + "?", "AskMultipleChoice"));
            }
            stage.setScene(new Scene(new VBox(panel), 500, 700));
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
            scrollPane[0] = (ScrollPane) panel.lookup(".questions-scroll-pane");
            content[0] = (VBox) scrollPane[0].getContent();
        });

        then(panel.getHeight()).isLessThanOrEqualTo(500);
        then(scrollPane[0].getViewportBounds().getHeight()).isLessThan(content[0].getLayoutBounds().getHeight());
    }

    @Test
    void questions_panel_uses_only_the_height_needed_for_short_content() {
        QuestionsPane panel = new QuestionsPane();

        interact(() -> {
            panel.questions.add(question("A short question?", "AskMultipleChoice"));
            stage.setScene(new Scene(new VBox(panel), 500, 700));
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
        });

        then(panel.getHeight()).isLessThan(500);
    }

    @Test
    void confirmation_question_fits_without_internal_scrolling() {
        QuestionsPane panel = new QuestionsPane();
        ScrollPane[] scrollPane = new ScrollPane[1];
        VBox[] content = new VBox[1];

        interact(() -> {
            panel.questions.add(CONFIRMATION);
            stage.setScene(new Scene(new VBox(panel), 500, 700));
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
            scrollPane[0] = (ScrollPane) panel.lookup(".questions-scroll-pane");
            content[0] = (VBox) scrollPane[0].getContent();
        });

        then(content[0].getLayoutBounds().getHeight())
            .isLessThanOrEqualTo(scrollPane[0].getViewportBounds().getHeight());
    }

    @Test
    void question_panel_max_width_is_defined_by_fxml() {
        QuestionsPane panel = new QuestionsPane();

        then(panel.getMaxWidth()).isEqualTo(Double.POSITIVE_INFINITY);
    }

    private QuestionsPane.Question question(String text, String toolName) {
        return new QuestionsPane.Question(new ToolInvocationMessage(
            new InvocationJSONObject(toolName, new JSONObject(Map.of(
                "question", text,
                "options", List.of("Web", "Mobile", "Desktop")
            )))
        ));
    }
}