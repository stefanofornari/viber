# Notes for US-000033 — GUI AskTools Implementation

## Status
Implementation complete. Questions are now displayed in a QuestionsPanel within the chat window instead of modal dialogs.

## Changes Made

### New Components
1. **QuestionsPanel** (`src/main/java/ste/ai/viber/renderer/QuestionsPanel.java` + FXML)
   - Displays multiple questions (open-ended, single choice, multiple choice, confirmation) in a single panel
   - Each question has appropriate input control (TextArea, ChoiceBox, CheckBox list, Yes/No selector)
   - Confirm/Cancel buttons at bottom
   - Returns answers via CompletableFuture when user confirms

2. **QuestionsPanelManager** (`src/main/java/ste/ai/viber/renderer/QuestionsPanelManager.java`)
   - Singleton managing QuestionsPanel lifecycle
   - Coordinates between tool invocations (ViberGUIAskTools) and UI (ChatPaneController)
   - Collects all received questions and shows them together in one panel (no limit)
   - Implements `QuestionsPanelProvider` interface for testability

3. **QuestionsPanelProvider** (`src/main/java/ste/ai/viber/renderer/QuestionsPanelProvider.java`)
   - Interface for dependency injection and testing

### Modified Components
1. **ViberGUIAskTools** (`src/main/java/ste/ai/viber/tools/ViberGUIAskTools.java`)
   - Replaced modal dialogs with QuestionsPanelManager calls
   - Tool methods now block on CompletableFuture until user answers (no timeout)
   - Added backward-compatible constructor accepting Window (deprecated)
   - Added package-private constructor for testing with mock provider

2. **ChatPaneController** (`src/main/java/ste/ai/viber/renderer/ChatPaneController.java`)
   - Detects AskTools invocations (tool names starting with "Ask")
   - Sets up QuestionsPanelManager consumer to add panels to chat
   - Skips ToolMessagePane creation for AskTools
   - Handles ToolExecutionMessage for AskTools by marking panel as answered

3. **ViberGUIAskToolsTest** (`src/test/java/ste/ai/viber/tools/ViberGUIAskToolsTest.java`)
   - Rewritten to use mock QuestionsPanelProvider
   - All 20 tests pass

## Flow
1. User requests scoping → LLM calls AskQuestion/AskSingleChoice/etc.
2. ViberGUIAskTools calls QuestionsPanelManager → creates QuestionsPanel via consumer
3. ChatPaneController adds QuestionsPanel to chat messages
4. User answers questions in panel → clicks Confirm
5. QuestionsPanel completes all futures → tool methods return answers
6. LangChain4j sends ToolExecutionMessage with answers
7. ChatPaneController marks panel as answered

## Key Design Decisions
- **No timeout**: Answers are delivered by a user-initiated message, so tool methods wait indefinitely without resource waste.
- **No question limit**: All questions from a single LLM response are displayed in the panel. The LLM is instructed to limit questions per turn, but the UI handles any number.
- **Cancel handling**: When the user cancels a question, the tool informs the LLM that no answer was provided by throwing `dev.langchain4j.exception.ToolErrorVisibleToLlm`.
- **Pane removal**: Answering or cancelling a question removes that question row; the whole pane is removed from its container when no questions remain.
- **Chat display**: AskTools tool execution results (answer or "No answer provided") are shown in the chat as a ToolExecutionMessage.

## Testing
- All 158 tests pass (including 20 ViberGUIAskToolsTest)
- No regressions detected

## References
- Story: `uzz/stories/scope/US-000033/US-000033.md`
- Acceptance criteria: Questions displayed in chat window (not modal dialogs), Confirm/Cancel buttons