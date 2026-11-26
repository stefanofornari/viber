package ste.ai.viber.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import ste.ai.viber.actor.FXActor;
import ste.ai.viber.renderer.GUIRenderer;
import ste.ai.viber.storyeditor.UserStory;
import ste.llm.chat.model.Conversation;

import static org.assertj.core.api.BDDAssertions.then;

@org.junit.jupiter.api.extension.ExtendWith(TestNameLogger.class)
class DevModeStoryEditorTest extends ApplicationTest {

    private static final String STORY = """
        ## US-000042 — From file

        ### User Story

        ```
        AS A Developer
        I WANT a file
        SO THAT I test
        ```

        #### Status
        TODO
        """;

    @TempDir
    Path dir;

    private DevModeController controller;

    @Override
    public void start(final Stage stage) throws Exception {
        final GUIRenderer renderer = new GUIRenderer();
        final FXActor fxActor = new FXActor(new Conversation(), renderer);
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("DevModeWindow.fxml"));
        final Parent root = loader.load();
        controller = loader.getController();
        controller.initialize(fxActor, fxActor.conversation(), renderer, Collections.emptyList());
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    private void click(final javafx.scene.control.Button button) {
        interact(button::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void dev_mode_window_contains_the_story_editor() {
        then(controller.storyEditor).isNotNull();
        then(controller.storyEditor.story()).isNull();
    }

    @Test
    void open_shows_the_chosen_story_file() throws IOException {
        final Path file = Files.writeString(dir.resolve("US-000042.md"), STORY);
        controller.storyFileOpener = () -> file;

        click(controller.openStoryButton);

        then(controller.storyEditor.story().id()).isEqualTo("US-000042");
        then(controller.storyErrorLabel.getText()).isEmpty();
    }

    @Test
    void cancelled_open_changes_nothing() {
        controller.storyFileOpener = () -> null;

        click(controller.openStoryButton);

        then(controller.storyEditor.story()).isNull();
    }

    @Test
    void save_writes_the_edits_to_the_opened_file() throws IOException {
        final Path file = Files.writeString(dir.resolve("US-000042.md"), STORY);
        controller.storyFileOpener = () -> file;
        click(controller.openStoryButton);
        interact(() -> controller.storyEditor.story().title("Edited title"));

        click(controller.saveStoryButton);

        then(Files.readString(file)).contains("## US-000042 — Edited title");
    }

    @Test
    void save_of_a_story_without_file_asks_for_the_target() throws IOException {
        final Path target = dir.resolve("new.md");
        controller.storyFileSaver = () -> target;
        click(controller.sampleStoryButton);

        click(controller.saveStoryButton);

        then(target).exists();
        then(Files.readString(target)).contains(controller.storyEditor.story().id());
    }

    @Test
    void sample_llm_proposal_is_shown_as_the_current_story() {
        click(controller.sampleStoryButton);

        then(controller.storyEditor.story()).isNotNull();
        then(controller.storyEditor.story().scenarios()).isNotEmpty();
        then(controller.storyErrorLabel.getText()).isEmpty();
    }

    @Test
    void invalid_file_shows_an_error_and_keeps_the_content() throws IOException {
        click(controller.sampleStoryButton);
        final UserStory before = controller.storyEditor.story();
        final Path bad = Files.writeString(dir.resolve("bad.md"), "not a story");
        controller.storyFileOpener = () -> bad;

        click(controller.openStoryButton);

        then(controller.storyErrorLabel.getText()).isNotEmpty();
        then(controller.storyEditor.story()).isSameAs(before);
    }
}
