# Notes for US-000064 — UserStory JavaFX Model with Markdown Round-Trip

## Design decisions

- Package `ste.ai.viber.storyeditor`: `UserStory` (model), `UserStoryStatus`, `UserStoryMarkdown` (parse/render), `UserStoryParseException`.
- `UserStory.toMarkdown()` delegates to `UserStoryMarkdown`; there is no static `fromMarkdown`, per the no-static rule — use `new UserStoryMarkdown().parse(text)`.
- **References and keywords are modelled too** (observable lists), beyond the field list in the story, so that load → save never drops them.
- Text properties are never null (empty string); `status(null)` resets to `TODO`.
- Scenarios are stored as full text (title + steps); technical context and notes as raw text.
- Parsing is lenient on missing optional sections (status defaults to `TODO`) and strict on the title heading (`## ID — Title`), the three `AS A / I WANT / SO THAT` lines and the status value.
- Section detection ignores headings inside code fences, so fenced examples in notes are safe.
- `Dependencies: - None` parses to an empty list; an empty list renders as `- None`.
- Parse errors are `UserStoryParseException` (an `IllegalArgumentException`).
- Round trip is stable at model level (parse(render(s)) equals s); whitespace/blank-line layout of the original file is normalized, not preserved.
