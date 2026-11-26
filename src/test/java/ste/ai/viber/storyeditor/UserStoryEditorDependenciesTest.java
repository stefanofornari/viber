package ste.ai.viber.storyeditor;

import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import ste.ai.viber.test.FXTestSupport;

import static org.assertj.core.api.BDDAssertions.then;

@org.junit.jupiter.api.extension.ExtendWith(ste.ai.viber.cli.TestNameLogger.class)
class UserStoryEditorDependenciesTest extends ApplicationTest {

    private UserStoryEditor editor;

    @Override
    public void start(final Stage stage) {
        editor = new UserStoryEditor();
        stage.setScene(FXTestSupport.styled(new Scene(editor, 700, 600)));
        stage.show();
    }

    private TextField field() {
        return lookup("#dependenciesField").queryAs(TextField.class);
    }

    private void show(final UserStory story) {
        interact(() -> editor.story(story));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void shows_the_dependencies_as_a_comma_separated_list() {
        final UserStory story = new UserStory();
        story.dependencies().addAll("US-000001", "US-000002");

        show(story);

        then(field().getText()).isEqualTo("US-000001, US-000002");
    }

    @Test
    void typed_text_updates_the_model() {
        final UserStory story = new UserStory();
        show(story);

        clickOn("#dependenciesField").write("US-000010");

        then(story.dependencies()).containsExactly("US-000010");
    }

    @Test
    void entries_are_split_on_commas_and_trimmed() {
        final UserStory story = new UserStory();
        show(story);

        clickOn("#dependenciesField").write("  US-000001 ,US-000002,   US-000003  ");

        then(story.dependencies()).containsExactly("US-000001", "US-000002", "US-000003");
    }

    @Test
    void blank_and_duplicate_entries_are_ignored() {
        final UserStory story = new UserStory();
        show(story);

        clickOn("#dependenciesField").write("US-000010, , US-000010,,US-000011");

        then(story.dependencies()).containsExactly("US-000010", "US-000011");
    }

    @Test
    void clearing_the_text_removes_all_dependencies() {
        final UserStory story = new UserStory();
        story.dependencies().add("US-000010");
        show(story);

        interact(() -> field().clear());

        then(story.dependencies()).isEmpty();
    }

    @Test
    void model_changes_update_the_text() {
        final UserStory story = new UserStory();
        show(story);

        interact(() -> story.dependencies().add("US-000010"));

        then(field().getText()).isEqualTo("US-000010");
    }

    @Test
    void typing_a_trailing_comma_does_not_rewrite_the_text() {
        final UserStory story = new UserStory();
        show(story);

        clickOn("#dependenciesField").write("US-000010,");

        then(field().getText()).isEqualTo("US-000010,");
    }

    @Test
    void replaced_story_is_not_modified_by_later_typing() {
        final UserStory first = new UserStory();
        show(first);
        show(new UserStory());

        clickOn("#dependenciesField").write("US-000010");

        then(first.dependencies()).isEmpty();
    }

    @Test
    void field_is_empty_and_disabled_without_a_story() {
        then(field().getText()).isEmpty();
        then(field().isDisabled()).isTrue();
    }
}
