package ste.ai.viber.storyeditor;

import java.util.List;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import ste.ai.viber.test.FXTestSupport;

import static org.assertj.core.api.BDDAssertions.then;

@org.junit.jupiter.api.extension.ExtendWith(ste.ai.viber.cli.TestNameLogger.class)
class UserStoryEditorScenariosTest extends ApplicationTest {

    private UserStoryEditor editor;

    @Override
    public void start(final Stage stage) {
        editor = new UserStoryEditor();
        stage.setScene(FXTestSupport.styled(new Scene(editor, 700, 600)));
        stage.show();
    }

    private UserStory show(final String... scenarios) {
        final UserStory story = new UserStory();
        story.scenarios().addAll(scenarios);
        interact(() -> editor.story(story));
        interact(() -> ((javafx.scene.control.TabPane) lookup("#tabs").query()).getSelectionModel().select(1));
        WaitForAsyncUtils.waitForFxEvents();
        return story;
    }

    private List<TitledPane> panes() {
        return lookup(".scenario-pane").queryAllAs(TitledPane.class).stream().toList();
    }

    private List<String> titles() {
        return lookup(".scenario-title").queryAllAs(Label.class).stream().map(Label::getText).toList();
    }

    private void press(final int index, final String styleClass) {
        interact(() -> ((Button) panes().get(index).lookup("." + styleClass)).fire());
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void expand(final int index) {
        interact(() -> panes().get(index).setExpanded(true));
    }

    @Test
    void scenarios_start_collapsed_showing_only_the_title() {
        show("Scenario: A\n  Given x", "Scenario: B\n  Given y");

        then(titles()).containsExactly("Scenario: A", "Scenario: B");
        then(panes()).extracting(TitledPane::isExpanded).containsOnly(false);
    }

    @Test
    void scenarios_are_in_a_scrollable_pane() {
        show("Scenario: A");

        then(lookup("#scenarioScroll").queryAs(ScrollPane.class).getContent())
            .isSameAs(lookup("#scenarioBox").query());
    }

    @Test
    void add_a_scenario() {
        final UserStory story = show("Scenario: A", "Scenario: B");

        clickOn("#addScenarioButton");

        then(story.scenarios()).hasSize(3);
        then(story.scenarios().get(2)).startsWith("Scenario:").contains("Given", "When", "Then");
        then(panes().get(2).isExpanded()).isTrue();
    }

    @Test
    void edit_a_scenario() {
        final UserStory story = show("Scenario: A", "Scenario: B");
        expand(0);

        interact(() -> ((TextArea) panes().get(0).getContent()).setText("Scenario: Changed"));

        then(story.scenarios()).containsExactly("Scenario: Changed", "Scenario: B");
        then(titles().get(0)).isEqualTo("Scenario: Changed");
    }

    @Test
    void delete_a_scenario_leaves_an_undo() {
        final UserStory story = show("Scenario: A", "Scenario: B");

        press(0, "scenario-delete-button");

        then(story.scenarios()).containsExactly("Scenario: B");
        then(lookup(".scenario-undo-button").queryAll()).hasSize(1);
        then(lookup(".scenario-delete-button").queryAll()).hasSize(1);
    }

    @Test
    void undo_restores_the_scenario_at_its_position() {
        final UserStory story = show("Scenario: A", "Scenario: B", "Scenario: C");
        press(1, "scenario-delete-button");

        interact(() -> lookup(".scenario-undo-button").queryAs(Button.class).fire());
        WaitForAsyncUtils.waitForFxEvents();

        then(story.scenarios()).containsExactly("Scenario: A", "Scenario: B", "Scenario: C");
        then(lookup(".scenario-undo-button").queryAll()).isEmpty();
    }

    @Test
    void a_deleted_scenario_stays_listed_collapsed_and_is_not_saved() {
        final UserStory story = show("Scenario: A", "Scenario: B");
        expand(0);

        press(0, "scenario-delete-button");

        then(panes()).hasSize(2);
        then(panes().get(0).isExpanded()).isFalse();

        expand(0);
        then(((TextArea) panes().get(0).getContent()).getText()).isEqualTo("Scenario: A");
        then(((TextArea) panes().get(0).getContent()).isEditable()).isFalse();
        then(story.toMarkdown()).doesNotContain("Scenario: A").contains("Scenario: B");
    }

    @Test
    void a_deleted_scenario_survives_other_changes() {
        final UserStory story = show("Scenario: A", "Scenario: B", "Scenario: C");
        press(0, "scenario-delete-button");

        press(2, "scenario-up-button");
        clickOn("#addScenarioButton");

        then(titles()).startsWith("Scenario: A", "Scenario: C", "Scenario: B");
        then(lookup(".scenario-undo-button").queryAll()).hasSize(1);
        then(story.scenarios()).hasSize(3).doesNotContain("Scenario: A");
    }

    @Test
    void up_moves_a_scenario_one_position_up() {
        final UserStory story = show("Scenario: A", "Scenario: B", "Scenario: C");

        press(2, "scenario-up-button");

        then(story.scenarios()).containsExactly("Scenario: A", "Scenario: C", "Scenario: B");
        then(titles()).containsExactly("Scenario: A", "Scenario: C", "Scenario: B");
    }

    @Test
    void up_on_the_first_scenario_moves_it_to_the_last_position() {
        final UserStory story = show("Scenario: A", "Scenario: B", "Scenario: C");

        press(0, "scenario-up-button");

        then(story.scenarios()).containsExactly("Scenario: B", "Scenario: C", "Scenario: A");
    }

    @Test
    void moving_keeps_the_expanded_state_with_the_scenario() {
        show("Scenario: A", "Scenario: B");
        expand(1);

        press(1, "scenario-up-button");

        then(panes()).extracting(TitledPane::isExpanded).containsExactly(true, false);
    }

    @Test
    void scenario_panes_stay_within_the_viewport_width() {
        show("Scenario: A", "Scenario: B");
        final ScrollPane scroll = lookup("#scenarioScroll").queryAs(ScrollPane.class);

        WaitForAsyncUtils.waitForFxEvents();
        final double first = lookup("#scenarioBox").queryAs(javafx.scene.layout.VBox.class).getWidth();
        WaitForAsyncUtils.waitForFxEvents();
        sleep(500);

        then(first).isLessThanOrEqualTo(scroll.getViewportBounds().getWidth());
        then(lookup("#scenarioBox").queryAs(javafx.scene.layout.VBox.class).getWidth()).isEqualTo(first);
        then(panes().get(0).getWidth()).isLessThanOrEqualTo(scroll.getViewportBounds().getWidth());
    }
}
