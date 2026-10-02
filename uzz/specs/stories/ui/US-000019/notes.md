# Notes: US-000019 — GUI Prompt Input Area

## Technical Decisions

- **MainAppWindow as standalone application view**: A new FXML layout (`MainAppWindow.fxml`) wraps the existing `GUIRenderer` and adds a prompt input area (TextField + Send Button) below the conversation view.
- **MainAppController manages input and actor interaction**: The controller holds references to `Actor` and `GUIRenderer`. On send, it creates a new `Chat` with a `PromptMessage` and delegates to `actor.chat(chat, msg -> renderer.render(msg))`, mirroring how `ViberCLI` creates chats.
- **Enter key triggers send**: `inputField.setOnAction(e -> handleSend())` enables Enter-to-send behavior, matching user expectations for prompt input.
- **Empty input is ignored**: `text.isBlank()` check prevents creating empty messages, consistent with `ViberCLI` behavior.
- **Input cleared and refocused after send**: After sending, the input field is cleared and focus is returned for rapid successive prompts.
- **New chat per prompt**: Each sent prompt creates a new `Chat` (matching CLI behavior), which becomes the current chat. This keeps the model simple and aligns with the existing `ViberCLI` pattern.
- **Standalone mode prerequisite (US-000018)**: `ViberApplication` is modified so that running without CLI flags launches the `MainAppWindow` with `FXActor` as the default actor, instead of starting the CLI loop. The `--key` option is made optional to allow no-flag GUI launches.

## Trade-offs

- **Making `--key` optional**: Previously `--key` was required by picocli. It is now optional to allow standalone GUI launches without credentials. CLI mode with `LangChain4jActor` still requires `--key`; if missing in non-GUI mode, `createActor()` throws an `IllegalArgumentException` with a clear message.
- **One chat per prompt vs. continuing current chat**: The implementation creates a new chat for each prompt, matching CLI behavior. This is simpler than tracking a "current chat" for message continuation, which can be added later if needed.
- **ViberApplication as mixed CLI/FX entry point**: The same `Application` subclass handles both CLI and standalone FX modes. A future refactor could split them if the logic grows, but this keeps the MVP simple.

## References

- Coding Standard: `uzz/specs/coding-standard.md`
- Development Framework: `uzz/specs/development-framework.md`
- Scope: `uzz/specs/scope.md`
- Related US: `US-000018` (standalone entry point, implemented together)
