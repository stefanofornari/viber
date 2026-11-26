# Notes for US-000068

- The editor sections are tabs of a `TabPane` (`tabs`): Main, Acceptance criteria, Notes. Content of all tabs stays in the scene graph, so TestFX text lookups (`clickOn("Notes")`) can hit hidden nodes; tests select tabs through the selection model.
- Technical context and notes are plain `TextArea`s bound bidirectionally to the story; each tab's content is disabled when there is no story.
