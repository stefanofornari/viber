package ste.ai.viber.renderer;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.assertj.core.api.BDDAssertions.then;

import javafx.scene.control.TitledPane;
import java.util.List;

class QuestionsPanelTest extends ApplicationTest {

    @Test
    void questions_panel_is_titled_pane() {
        QuestionsPanel panel = new QuestionsPanel();
        then(panel).isInstanceOf(TitledPane.class);
    }

    @Test
    void questions_panel_has_correct_title_format() {
        QuestionsPanel panel = new QuestionsPanel();
        panel.setToolName("AskQuestion");
        then(panel.getText()).isEqualTo("ACTOR / AskQuestion");
    }

    @Test
    void questions_panel_can_be_instantiated_without_fxml_error() {
        QuestionsPanel panel = new QuestionsPanel();
        then(panel).isNotNull();
    }

    @Test
    void questions_panel_can_add_open_ended_question() {
        QuestionsPanel panel = new QuestionsPanel();
        panel.addOpenEndedQuestion("What is your name?");
        then(panel).isNotNull();
    }

    @Test
    void questions_panel_can_add_single_choice_question() {
        QuestionsPanel panel = new QuestionsPanel();
        panel.addSingleChoiceQuestion("Choose platform", List.of("Web", "Mobile", "Desktop"));
        then(panel).isNotNull();
    }

    @Test
    void questions_panel_can_add_multiple_choice_question() {
        QuestionsPanel panel = new QuestionsPanel();
        panel.addMultipleChoiceQuestion("Choose platforms", List.of("Web", "Mobile", "Desktop"));
        then(panel).isNotNull();
    }

    @Test
    void questions_panel_can_add_confirmation_question() {
        QuestionsPanel panel = new QuestionsPanel();
        panel.addConfirmationQuestion("Do you agree?");
        then(panel).isNotNull();
    }

    @Test
    void questions_panel_can_add_multiple_questions() {
        QuestionsPanel panel = new QuestionsPanel();
        panel.addOpenEndedQuestion("Question 1");
        panel.addSingleChoiceQuestion("Question 2", List.of("A", "B"));
        panel.addConfirmationQuestion("Question 3");
        then(panel).isNotNull();
    }
}