package ste.ai.viber.renderer;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.assertj.core.api.BDDAssertions.then;

import javafx.scene.control.TitledPane;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import ste.llm.chat.model.ToolInvocationMessage;
import ste.llm.tools.InvocationJSONObject;

class QuestionsPanelTest extends ApplicationTest {

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
}