package ste.ai.viber.storyeditor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ste.lazyjava.safe.Safe;

/**
 * Converts a {@link UserStory} to and from the uzz user story markdown format
 * described in {@code uzz/skills/kb/references/user_story.md}.
 *
 * <p>Parsing is structural only: no INVEST or Gherkin semantic validation is done.
 * Sections other than the title and the user story block are optional.
 */
public class UserStoryMarkdown implements Safe {

    private static final String SECTION_USER_STORY = "user story";
    private static final String SECTION_STATUS = "status";
    private static final String SECTION_DEPENDENCIES = "dependencies";
    private static final String SECTION_TECHNICAL = "technical context & constraints";
    private static final String SECTION_NOTES = "notes";
    private static final String SECTION_CRITERIA = "acceptance criteria";
    private static final String SECTION_REFERENCES = "references";
    private static final String SECTION_KEYWORDS = "keywords";

    private static final Pattern HEADING = Pattern.compile("^(#{2,4}) +(.*?) *$");
    private static final Pattern TITLE = Pattern.compile("^(\\S+) +— +(.+)$");
    private static final Pattern SCENARIO = Pattern.compile("^\\s*Scenario( Outline)?:.*");
    private static final String NONE = "None";

    /**
     * Parses a story from markdown.
     *
     * @param text the markdown text, not blank
     * @return the parsed story
     * @throws IllegalArgumentException if the text is null or blank
     * @throws UserStoryParseException if the text is not a valid story
     */
    public UserStory parse(final String text) {
        requireNonBlank(text, "text");

        final Map<String, List<String>> sections = new LinkedHashMap<>();
        String titleLine = null;
        List<String> current = null;
        boolean inFence = false;

        for (final String line : text.split("\\R", -1)) {
            if (line.stripLeading().startsWith("```")) {
                inFence = !inFence;
            }
            final Matcher heading = inFence ? null : HEADING.matcher(line);
            if (heading != null && heading.matches()) {
                if (heading.group(1).length() == 2) {
                    if (titleLine == null) {
                        titleLine = heading.group(2);
                    }
                    current = null;
                } else {
                    current = new ArrayList<>();
                    sections.put(heading.group(2).toLowerCase(), current);
                }
                continue;
            }
            if (current != null) {
                current.add(line);
            }
        }

        if (titleLine == null) {
            throw new UserStoryParseException("missing story title heading (## ID — Title)");
        }
        final Matcher title = TITLE.matcher(titleLine);
        if (!title.matches()) {
            throw new UserStoryParseException("title heading must be 'ID — Title': " + titleLine);
        }

        final UserStory story = new UserStory();
        story.id(title.group(1));
        story.title(title.group(2));
        parseUserStory(story, sections.get(SECTION_USER_STORY));
        story.status(parseStatus(sections.get(SECTION_STATUS)));
        story.dependencies().setAll(items(sections.get(SECTION_DEPENDENCIES)).stream()
            .filter(item -> !NONE.equalsIgnoreCase(item)).toList());
        story.technicalContext(block(sections.get(SECTION_TECHNICAL)));
        story.notes(block(sections.get(SECTION_NOTES)));
        story.scenarios().setAll(scenarios(sections.get(SECTION_CRITERIA)));
        story.references().setAll(items(sections.get(SECTION_REFERENCES)));
        story.keywords().setAll(items(sections.get(SECTION_KEYWORDS)));
        return story;
    }

    /**
     * Renders a story as markdown following the uzz template.
     *
     * @param story the story to render
     * @return the markdown text, ending with a newline
     */
    public String render(final UserStory story) {
        requireNonNull(story, "story");

        final StringBuilder out = new StringBuilder();
        out.append("## ").append(story.id()).append(" — ").append(story.title()).append("\n\n");
        out.append("### User Story\n\n```\n");
        out.append("AS A ").append(story.role()).append('\n');
        out.append("I WANT ").append(story.want()).append('\n');
        out.append("SO THAT ").append(story.soThat()).append("\n```\n\n");
        out.append("#### Status\n").append(story.status()).append("\n\n");
        out.append("#### Dependencies\n");
        if (story.dependencies().isEmpty()) {
            out.append("- ").append(NONE).append('\n');
        } else {
            story.dependencies().forEach(d -> out.append("- ").append(d).append('\n'));
        }
        out.append("\n#### Technical Context & Constraints\n").append(story.technicalContext()).append("\n\n");
        out.append("#### Notes\n").append(story.notes()).append("\n\n");
        out.append("### Acceptance Criteria\n\n```gherkin\n");
        out.append(String.join("\n\n", story.scenarios())).append("\n```\n\n");
        out.append("### References\n");
        story.references().forEach(r -> out.append("- ").append(r).append('\n'));
        out.append("\n### Keywords\n");
        story.keywords().forEach(k -> out.append("- ").append(k).append('\n'));
        return out.toString();
    }

    private void parseUserStory(final UserStory story, final List<String> lines) {
        final String[] prefixes = {"AS A ", "I WANT ", "SO THAT "};
        final String[] values = new String[prefixes.length];
        int index = -1;

        for (final String line : lines == null ? List.<String>of() : lines) {
            if (line.startsWith("```")) {
                continue;
            }
            int matched = -1;
            for (int i = 0; i < prefixes.length; i++) {
                if (line.startsWith(prefixes[i])) {
                    matched = i;
                }
            }
            if (matched >= 0) {
                index = matched;
                values[index] = line.substring(prefixes[index].length());
            } else if (index >= 0 && !line.isBlank()) {
                values[index] += "\n" + line;
            }
        }
        for (int i = 0; i < prefixes.length; i++) {
            if (values[i] == null) {
                throw new UserStoryParseException("missing '" + prefixes[i].trim() + "' in the User Story block");
            }
        }
        story.role(values[0].strip());
        story.want(values[1].strip());
        story.soThat(values[2].strip());
    }

    private UserStoryStatus parseStatus(final List<String> lines) {
        final String value = block(lines);
        if (value.isEmpty()) {
            return UserStoryStatus.TODO;
        }
        try {
            return UserStoryStatus.valueOf(value.toUpperCase());
        } catch (final IllegalArgumentException x) {
            throw new UserStoryParseException("unknown status: " + value);
        }
    }

    private List<String> scenarios(final List<String> lines) {
        final List<StringBuilder> scenarios = new ArrayList<>();
        boolean inFence = false;
        for (final String line : lines == null ? List.<String>of() : lines) {
            if (line.stripLeading().startsWith("```")) {
                inFence = !inFence;
            } else if (inFence && SCENARIO.matcher(line).matches()) {
                scenarios.add(new StringBuilder(line));
            } else if (inFence && !scenarios.isEmpty()) {
                scenarios.get(scenarios.size() - 1).append('\n').append(line);
            }
        }
        return scenarios.stream().map(s -> s.toString().stripTrailing()).toList();
    }

    private List<String> items(final List<String> lines) {
        final List<String> items = new ArrayList<>();
        for (final String line : lines == null ? List.<String>of() : lines) {
            final String trimmed = line.strip();
            if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                items.add(trimmed.substring(2).strip());
            }
        }
        return items;
    }

    private String block(final List<String> lines) {
        return lines == null ? "" : String.join("\n", lines).strip();
    }
}
