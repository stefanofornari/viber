package ste.ai.viber.storyeditor;

import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import ste.ai.viber.test.FXTestSupport;

import static org.assertj.core.api.BDDAssertions.then;

@org.junit.jupiter.api.extension.ExtendWith(ste.ai.viber.cli.TestNameLogger.class)
class UserStoryEditorTest extends ApplicationTest {

    private UserStoryEditor editor;

    @Override
    public void start(final Stage stage) {
        editor = new UserStoryEditor();
        stage.setScene(FXTestSupport.styled(new Scene(editor, 700, 500)));
        stage.show();
    }

    private UserStory story() {
        final UserStory story = new UserStory();
        story.id("US-000001");
        story.title("A title");
        story.role("Developer");
        story.want("a thing");
        story.soThat("value");
        story.status(UserStoryStatus.WIP);
        return story;
    }

    private void show(final UserStory story) {
        interact(() -> editor.story(story));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void shows_the_header_fields_of_the_story() {
        show(story());

        then(lookup("#idField").queryAs(TextField.class).getText()).isEqualTo("US-000001");
        then(lookup("#titleField").queryAs(TextField.class).getText()).isEqualTo("A title");
        then(lookup("#roleField").queryAs(TextField.class).getText()).isEqualTo("Developer");
        then(lookup("#wantArea").queryAs(TextArea.class).getText()).isEqualTo("a thing");
        then(lookup("#soThatArea").queryAs(TextArea.class).getText()).isEqualTo("value");
        then(lookup("#statusBox").queryAs(ComboBox.class).getValue()).isEqualTo(UserStoryStatus.WIP);
    }

    @Test
    void status_dropdown_offers_all_statuses() {
        then(lookup("#statusBox").queryAs(ComboBox.class).getItems())
            .containsExactly((Object[]) UserStoryStatus.values());
    }

    @Test
    void editing_the_ui_updates_the_model() {
        final UserStory story = story();
        show(story);

        clickOn("#titleField").push(javafx.scene.input.KeyCode.END).write(" edited");
        clickOn("#wantArea").write(" more");
        interact(() -> lookup("#statusBox").queryAs(ComboBox.class).setValue(UserStoryStatus.DONE));

        then(story.title()).isEqualTo("A title edited");
        then(story.want()).isEqualTo("a thing more");
        then(story.status()).isEqualTo(UserStoryStatus.DONE);
    }

    @Test
    void model_changes_update_the_ui() {
        final UserStory story = story();
        show(story);

        interact(() -> story.want("changed"));

        then(lookup("#wantArea").queryAs(TextArea.class).getText()).isEqualTo("changed");
    }

    @Test
    void no_story_means_empty_and_disabled_fields() {
        then(editor.story()).isNull();
        then(lookup("#titleField").queryAs(TextField.class).getText()).isEmpty();
        then(lookup("#titleField").queryAs(TextField.class).isDisabled()).isTrue();
        then(lookup("#statusBox").queryAs(ComboBox.class).isDisabled()).isTrue();
    }

    @Test
    void fields_are_enabled_when_a_story_is_set_and_cleared_when_removed() {
        show(story());
        then(lookup("#titleField").queryAs(TextField.class).isDisabled()).isFalse();

        show(null);

        then(lookup("#titleField").queryAs(TextField.class).getText()).isEmpty();
        then(lookup("#titleField").queryAs(TextField.class).isDisabled()).isTrue();
    }

    @Test
    void replaced_story_is_no_longer_modified_by_the_ui() {
        final UserStory first = story();
        show(first);
        show(new UserStory());

        clickOn("#titleField").write("typed");

        then(first.title()).isEqualTo("A title");
    }
}
