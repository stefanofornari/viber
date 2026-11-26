package ste.ai.viber.storyeditor;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import ste.ai.viber.test.FXTestSupport;

import static org.assertj.core.api.BDDAssertions.then;

@org.junit.jupiter.api.extension.ExtendWith(ste.ai.viber.cli.TestNameLogger.class)
class UserStoryEditorNotesTest extends ApplicationTest {

    private UserStoryEditor editor;

    @Override
    public void start(final Stage stage) {
        editor = new UserStoryEditor();
        stage.setScene(FXTestSupport.styled(new Scene(editor, 700, 600)));
        stage.show();
    }

    private void show(final UserStory story) {
        interact(() -> editor.story(story));
        interact(() -> ((javafx.scene.control.TabPane) lookup("#tabs").query()).getSelectionModel().select(2));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private TextArea area(final String id) {
        return lookup(id).queryAs(TextArea.class);
    }

    @Test
    void sections_are_in_separate_tabs() {
        then(lookup(".tab").queryAll()).hasSize(3);
    }

    @Test
    void shows_technical_context_and_notes() {
        final UserStory story = new UserStory();
        story.technicalContext("JavaFX\n\nTestFX");
        story.notes("1. first");

        show(story);

        then(area("#technicalContextArea").getText()).isEqualTo("JavaFX\n\nTestFX");
        then(area("#notesArea").getText()).isEqualTo("1. first");
    }

    @Test
    void multi_line_typing_updates_the_model() {
        final UserStory story = new UserStory();
        show(story);

        clickOn("#notesArea").write("one").type(javafx.scene.input.KeyCode.ENTER).write("two");

        then(story.notes()).isEqualTo("one\ntwo");
    }

    @Test
    void empty_notes_are_editable() {
        show(new UserStory());

        then(area("#notesArea").getText()).isEmpty();
        then(area("#notesArea").isEditable()).isTrue();
        then(area("#notesArea").isDisabled()).isFalse();
    }
}
