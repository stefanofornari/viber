# Notes: US-000021 — Actor Selection Menu

## Technical Decisions

- **CheckMenuItem for visual selection**: `MainAppWindow.fxml` uses `CheckMenuItem` for `FXActor` and `LangChain4j Actor` menu items, providing a clear visual indicator of the active actor via checkmarks.
- **Guard against deselection**: The `setOnAction` handlers prevent unchecking the currently selected item by re-applying selection before switching.
- **LangChain4jActor switch deferred**: Switching to `LangChain4jActor` currently shows an information dialog stating that configuration is not yet implemented, and reverts selection to `FXActor`. This keeps the menu functional without incomplete behavior.
- **FXActor as default**: FXActor is the default on startup and uses the existing `conversation` and `renderer` passed to the controller.

## Trade-offs

- **Settings not implemented in this US**: The user correctly noted that an API-key-only dialog is incomplete without endpoint/model configuration. That functionality is deferred to `US-000022` (Actor Settings Configuration).
- **FXActor window**: The FXActor interface IS the `MainAppWindow` itself. No separate window is needed; the existing standalone GUI already serves as the FXActor interface.
- **Radio-button behavior with CheckMenuItem**: JavaFX `CheckMenuItem` doesn't enforce mutual exclusion automatically. The handlers implement this manually by re-selecting the previous item if the new selection is rejected.

## User Feedback Addressed

- Added visual checkmark to show selected actor
- Removed incomplete API-key-only dialog
- LangChain4jActor switch shows information alert until settings US is implemented

## References

- Coding Standard: `uzz/specs/coding-standard.md`
- Development Framework: `uzz/specs/development-framework.md`
- Scope: `uzz/specs/scope.md`
- Related US: `US-000018` (standalone entry point), `US-000022` (actor settings)
