# Notes for US-000040 — Dev Mode AskTools Menu Button

## Technical Approach

1. **FXML Changes**: Add a MenuButton to the button bar in `DevModeWindow.fxml` with four MenuItems for each AskTools type
2. **Controller Changes**:
   - Add @FXML fields for the MenuButton and MenuItems
   - Add handler methods for each MenuItem that insert the corresponding JSON template into the text area
   - Keep the MenuButton enabled independently of text area content
3. **JSON Templates**:
   - AskQuestion: `{"tool":"AskQuestion","arguments":{"question":""}}`
   - AskSingleChoice: `{"tool":"AskSingleChoice","arguments":{"question":"","options":[]}}`
   - AskMultipleChoice: `{"tool":"AskMultipleChoice","arguments":{"question":"","options":[]}}`
   - AskConfirmation: `{"tool":"AskConfirmation","arguments":{"question":""}}`

## Implementation Details

- The MenuButton remains enabled so an AskTool type can be selected at any time
- When a menu item is selected, the text area should be replaced with the template (not appended)
- The cursor should be positioned appropriately for the user to edit the question
- Do not show AskTools invocation panes in the chat; show answers as tool response messages
- Append consecutive AskTools answers to the same tool response pane

## Files to Modify
- `src/main/resources/ste/ai/viber/cli/DevModeWindow.fxml`
- `src/main/java/ste/ai/viber/cli/DevModeController.java`
- `src/test/java/ste/ai/viber/cli/DevModeControllerTest.java` (add tests)