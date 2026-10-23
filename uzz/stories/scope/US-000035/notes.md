# Notes for US-000035 — GUI Integration: ScopeAgent Option

## Changes Made
- Moved the scoping phase indicator label from inside the Actor menu (as a CustomMenuItem) to a plain Label positioned just below the chat input widget (TextArea) in the main JavaFX window.
- Updated `src/main/resources/ste/ai/viber/cli/MainAppWindow.fxml`:
  - Removed the `<CustomMenuItem>` containing `scopePhaseIndicator` from the Actor menu.
  - Added a `<Label fx:id="scopePhaseIndicator" style="-fx-padding: 0 10px;" />` after the `<TextArea fx:id="inputField" ... />`.
- The controller (`MainAppController.java`) remains unchanged; it continues to update the label's text and style when the scoping phase is active or inactive.

## Rationale
The original placement of the scoping phase indicator within a menu was not obvious to users. Placing it directly below the chat input widget makes the scoping phase status more visible and associated with the current conversation context.

## Testing
- Ran the full test suite (`mvn test`); all 158 tests passed, indicating no regression.

## References
- User story description: `uzz/stories/scope/US-000035/US-000035.md`
- Acceptance criteria: visual indication distinguishes scoping phase from general chat.