# Notes for US-000075

- `DevModeWindow.fxml` root is now a `TabPane`: "Conversation" (previous content, unchanged) and "User Story" (Open, Save, Load LLM Proposal Sample, error label, `UserStoryEditor`).
- `Save` writes to the file last opened or saved; for a story without file (e.g. the sample) it asks for a target.
- File choosers are behind package-private `storyFileOpener` / `storyFileSaver` suppliers so tests don't open native dialogs.
- The "unsaved indicator clears" clause was removed from the Save scenario: the indicator is US-000072, not yet implemented.
