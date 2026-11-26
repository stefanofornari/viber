# Notes for US-000067

- Scenarios live in a `ScrollPane` (`scenarioScroll`) holding one `TitledPane` per scenario, collapsed by default; the header is the scenario's first line, the content a `TextArea`.
- Per-scenario actions live in the pane header (title, delete, up). The editor keeps its own rows, including those flagged as deleted; the story only holds the non-deleted ones, so deleted scenarios are never saved but stay listed (collapsed, struck through, with an undo button; it can still be expanded to read its content, read-only) and keep their position through moves and additions. Replacing the story or changing its scenarios from outside rebuilds the rows and discards deleted ones.
- Expanded state is kept in a list parallel to the model so add/remove/move keep it. Text edits set the model item without rebuilding the panes (guarded by `editingScenario`) to preserve focus.
