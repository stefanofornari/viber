# Notes for US-000041 — Dev Mode AskTools JSON Validation and Error Handling

## Technical Approach

1. **Validation Logic**: Create a validation method that checks:
   - Valid JSON syntax
   - Presence of "tool" field with valid AskTools name
   - Presence of "arguments" object
   - Required fields per tool type (question for all, options array for choice types)

2. **Integration**: Call validation in each message action button handler before creating messages
3. **Error Reporting**: Use existing pattern `fxActor.addError("Error message")` for validation failures

## Validation Rules
- AskQuestion: requires "tool"="AskQuestion", "arguments" object with "question" string
- AskSingleChoice: requires "tool"="AskSingleChoice", "arguments" object with "question" string and "options" array
- AskMultipleChoice: requires "tool"="AskMultipleChoice", "arguments" object with "question" string and "options" array
- AskConfirmation: requires "tool"="AskConfirmation", "arguments" object with "question" string

## Files to Modify
- `src/main/java/ste/ai/viber/cli/DevModeController.java` (add validation method and integrate in handlers)
- `src/test/java/ste/ai/viber/cli/DevModeControllerTest.java` (add validation tests)

## Implementation Details
- Reuse the existing JSONObject parsing from handleAddToolInvocation()
- Follow the same error handling pattern: catch Exception, call fxActor.addError(), clear input
- Validation should happen in all button handlers (addChat, addReply, addThought, addToolExecution, addToolReply, addError)