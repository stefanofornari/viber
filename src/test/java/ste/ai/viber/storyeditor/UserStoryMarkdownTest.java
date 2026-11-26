package ste.ai.viber.storyeditor;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.ListChangeListener;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

public class UserStoryMarkdownTest {

    private static final String STORY = """
        ## US-000064 — Sample Story

        ### User Story

        ```
        AS A Developer
        I WANT a model
        SO THAT I can bind it
        ```

        #### Status
        WIP

        #### Dependencies
        - US-000001
        - US-000002

        #### Technical Context & Constraints
        * **Target System**: Java
        * **Toolchain / Rules**: JUnit 5

        #### Notes
        1. First note, with a fenced example:
        ```
        #### Notes
        ```

        ### Acceptance Criteria

        ```gherkin
        Scenario: Happy path
          Given a state
          When  an action
          Then  an outcome

        Scenario: Edge case
          Given another state
          When  a boundary
          Then  an error
        ```

        ### References
        - [Scope](../scope.md)

        ### Keywords
        - model
        - markdown
        """;

    private final UserStoryMarkdown markdown = new UserStoryMarkdown();

    @Test
    public void parse_populates_all_fields() {
        final UserStory story = markdown.parse(STORY);

        then(story.id()).isEqualTo("US-000064");
        then(story.title()).isEqualTo("Sample Story");
        then(story.role()).isEqualTo("Developer");
        then(story.want()).isEqualTo("a model");
        then(story.soThat()).isEqualTo("I can bind it");
        then(story.status()).isEqualTo(UserStoryStatus.WIP);
        then(story.dependencies()).containsExactly("US-000001", "US-000002");
        then(story.technicalContext()).startsWith("* **Target System**: Java").endsWith("JUnit 5");
        then(story.notes()).startsWith("1. First note").contains("#### Notes");
        then(story.scenarios()).hasSize(2);
        then(story.scenarios().get(0)).startsWith("Scenario: Happy path").endsWith("Then  an outcome");
        then(story.scenarios().get(1)).startsWith("Scenario: Edge case");
        then(story.references()).containsExactly("[Scope](../scope.md)");
        then(story.keywords()).containsExactly("model", "markdown");
    }

    @Test
    public void round_trip_preserves_the_story() {
        final UserStory original = markdown.parse(STORY);

        final UserStory copy = markdown.parse(markdown.render(original));

        then(copy).isEqualTo(original);
        then(markdown.render(copy)).isEqualTo(markdown.render(original));
    }

    @Test
    public void round_trip_preserves_empty_optional_sections() {
        final UserStory original = new UserStory();
        original.id("US-000001");
        original.title("Minimal");
        original.role("User");
        original.want("a thing");
        original.soThat("value");

        final UserStory copy = markdown.parse(markdown.render(original));

        then(copy).isEqualTo(original);
        then(copy.dependencies()).isEmpty();
        then(copy.status()).isEqualTo(UserStoryStatus.TODO);
    }

    @Test
    public void dependencies_none_means_no_dependencies() {
        final UserStory story = markdown.parse(STORY.replace("- US-000001\n- US-000002", "- None"));

        then(story.dependencies()).isEmpty();
    }

    @Test
    public void multi_line_want_is_preserved() {
        final UserStory story = markdown.parse(STORY.replace("I WANT a model", "I WANT a model\nthat is observable"));

        then(story.want()).isEqualTo("a model\nthat is observable");
        then(markdown.parse(markdown.render(story))).isEqualTo(story);
    }

    @Test
    public void malformed_markdown_without_user_story_block_is_rejected() {
        final String malformed = STORY.replace("AS A Developer\n", "")
            .replace("I WANT a model\n", "")
            .replace("SO THAT I can bind it\n", "");

        thenThrownBy(() -> markdown.parse(malformed))
            .isInstanceOf(UserStoryParseException.class)
            .hasMessageContaining("AS A");
    }

    @Test
    public void missing_title_heading_is_rejected() {
        thenThrownBy(() -> markdown.parse("just some text"))
            .isInstanceOf(UserStoryParseException.class)
            .hasMessageContaining("title");
    }

    @Test
    public void unknown_status_is_rejected() {
        thenThrownBy(() -> markdown.parse(STORY.replace("WIP", "MAYBE")))
            .isInstanceOf(UserStoryParseException.class)
            .hasMessageContaining("MAYBE");
    }

    @Test
    public void null_or_blank_input_is_rejected() {
        thenThrownBy(() -> markdown.parse(null)).isInstanceOf(IllegalArgumentException.class);
        thenThrownBy(() -> markdown.parse("  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void story_to_markdown_delegates_to_renderer() {
        final UserStory story = markdown.parse(STORY);

        then(story.toMarkdown()).isEqualTo(markdown.render(story));
    }

    @Test
    public void properties_are_observable() {
        final UserStory story = new UserStory();
        final List<String> changes = new ArrayList<>();
        story.titleProperty().addListener((obs, oldValue, newValue) -> changes.add(oldValue + "->" + newValue));

        story.title("A");
        story.title("B");

        then(changes).containsExactly("->A", "A->B");
    }

    @Test
    public void list_properties_are_observable() {
        final UserStory story = new UserStory();
        final List<Object> events = new ArrayList<>();
        story.dependencies().addListener((ListChangeListener<String>) events::add);
        story.scenarios().addListener((ListChangeListener<String>) events::add);

        story.dependencies().add("US-000001");
        story.scenarios().add("Scenario: x");

        then(events).hasSize(2);
    }

    @Test
    public void stories_with_different_content_are_not_equal() {
        final UserStory a = markdown.parse(STORY);
        final UserStory b = markdown.parse(STORY);
        b.scenarios().remove(1);

        then(a).isNotEqualTo(b);
    }
}
