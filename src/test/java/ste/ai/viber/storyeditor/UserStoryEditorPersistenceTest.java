package ste.ai.viber.storyeditor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.assertj.core.api.BDDAssertions.then;

@org.junit.jupiter.api.extension.ExtendWith(ste.ai.viber.cli.TestNameLogger.class)
class UserStoryEditorPersistenceTest extends ApplicationTest {

    private static final String MARKDOWN = """
        ## US-000009 — Persisted

        ### User Story

        ```
        AS A Developer
        I WANT to load
        SO THAT I can edit
        ```

        #### Status
        DONE

        #### Dependencies
        - US-000001

        ### Acceptance Criteria

        ```gherkin
        Scenario: One
          Given x
        ```
        """;

    @TempDir
    Path dir;

    private UserStoryEditor editor;

    @Override
    public void start(final Stage stage) {
        editor = new UserStoryEditor();
        stage.setScene(new Scene(editor, 600, 400));
        stage.show();
    }

    private void fx(final Runnable action) {
        interact(action);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void load_from_file_shows_the_story() throws IOException {
        final Path file = Files.writeString(dir.resolve("US-000009.md"), MARKDOWN, StandardCharsets.UTF_8);

        fx(() -> editor.load(file));

        then(editor.story().id()).isEqualTo("US-000009");
        then(editor.story().status()).isEqualTo(UserStoryStatus.DONE);
        then(editor.error()).isNull();
    }

    @Test
    void load_from_string_shows_the_story() {
        fx(() -> editor.load(MARKDOWN));

        then(editor.story().title()).isEqualTo("Persisted");
        then(editor.story().scenarios()).hasSize(1);
    }

    private UserStoryEditor newEditor() throws Exception {
        return WaitForAsyncUtils.asyncFx(UserStoryEditor::new).get();
    }

    @Test
    void saved_story_can_be_loaded_by_another_editor() throws Exception {
        final Path file = dir.resolve("saved.md");
        fx(() -> editor.load(MARKDOWN));
        fx(() -> editor.story().title("Edited"));
        fx(() -> editor.save(file));

        final UserStoryEditor other = newEditor();
        fx(() -> other.load(file));

        then(other.story()).isEqualTo(editor.story());
        then(other.story().title()).isEqualTo("Edited");
    }

    @Test
    void to_markdown_renders_the_current_story() {
        fx(() -> editor.load(MARKDOWN));

        then(editor.toMarkdown()).isEqualTo(editor.story().toMarkdown());
    }

    @Test
    void to_markdown_without_story_is_empty() {
        then(editor.toMarkdown()).isEmpty();
    }

    @Test
    void malformed_file_reports_a_parse_error_and_keeps_the_story() throws IOException {
        fx(() -> editor.load(MARKDOWN));
        final UserStory before = editor.story();
        final Path bad = Files.writeString(dir.resolve("bad.md"), "not a story");

        fx(() -> editor.load(bad));

        then(editor.error()).isInstanceOf(UserStoryParseException.class);
        then(editor.story()).isSameAs(before);
        then(editor.story().title()).isEqualTo("Persisted");
    }

    @Test
    void malformed_string_reports_a_parse_error() {
        fx(() -> editor.load("not a story"));

        then(editor.error()).isInstanceOf(UserStoryParseException.class);
        then(editor.story()).isNull();
    }

    @Test
    void missing_file_reports_an_io_error() {
        fx(() -> editor.load(dir.resolve("missing.md")));

        then(editor.error()).isInstanceOf(IOException.class);
    }

    @Test
    void unwritable_path_reports_an_io_error() {
        fx(() -> editor.load(MARKDOWN));

        fx(() -> editor.save(dir.resolve("no/such/dir/story.md")));

        then(editor.error()).isInstanceOf(IOException.class);
    }

    @Test
    void saving_without_a_story_reports_an_error() {
        fx(() -> editor.save(dir.resolve("none.md")));

        then(editor.error()).isInstanceOf(IllegalStateException.class);
        then(dir.resolve("none.md")).doesNotExist();
    }

    @Test
    void successful_operation_clears_the_previous_error() {
        fx(() -> editor.load("not a story"));
        then(editor.error()).isNotNull();

        fx(() -> editor.load(MARKDOWN));

        then(editor.error()).isNull();
    }

    @Test
    void error_property_notifies_the_host() {
        final java.util.List<Exception> errors = new java.util.ArrayList<>();
        editor.errorProperty().addListener((obs, oldValue, newValue) -> errors.add(newValue));

        fx(() -> editor.load("not a story"));

        then(errors).hasSize(1);
    }
}
