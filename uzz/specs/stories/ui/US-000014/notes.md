# Notes: US-000014 — JavaFX Conversation Output Rendering

## Technical Decisions

- **Renderer interface reuse**: `GUIRenderer` implements the existing `Renderer` interface alongside `StringRenderer`, providing a JavaFX window-based output instead of text output.
- **FXML per model element**: Each model element (`Conversation`, `Chat`, `Message`) has its own FXML descriptor in `ste.ai.viber.renderer`, loaded via `FXMLLoader`.
- **Controller-per-view**: Each FXML has a dedicated controller (`ConversationPaneController`, `ChatPaneController`, `MessagePaneController`) with package-private methods for testability.
- **Message type distinction**: Message types are distinguished via pattern-matching switch in `MessagePaneController`, applying type-specific CSS style classes (e.g., `message-prompt`, `message-reply`) and including the type name in the TitledPane title.
- **WebView for message content**: `MessagePane` uses `WebView` to render message content as HTML, with a simple inline stylesheet for font styling. HTML is escaped to prevent injection.
- **Empty state**: An empty `Conversation` renders a `Label` with text `(empty conversation)` wrapped in a `StackPane` with the `empty-state` style class.
- **Async rendering**: `GUIRenderer.render()` schedules UI creation on the JavaFX Application Thread via `Platform.runLater`, allowing safe calls from non-FX threads (e.g., actor callbacks).
- **Testability**: `createConversationNode(Chat, int)` and `createMessageNode(ChatMessage)` are package-private to allow direct unit testing without showing stages. Tests use TestFX `interact()` to run FX-dependent code on the FX thread.
- **AtlantaFX styles**: CSS uses AtlantaFX-inspired visual language (muted backgrounds, colored left borders) while staying compatible with standard JavaFX CSS.

## Trade-offs

- `WebView` adds weight but enables future markdown/HTML rendering. For plain text, a `Label` or `TextArea` would be lighter.
- `derive()` CSS function is used in `viber.css` matching the existing project convention, though it is not standard JavaFX CSS.
- `GUIRenderer` always creates a new `Stage` per `render()` call. An embeddable variant would return a `Parent` instead; that is covered separately by the embeddable component story.
- `Platform.runLater` in `render()` makes the method asynchronous. Consumers must not assume the window is shown when the call returns.

## References

- Coding Standard: `uzz/specs/coding-standard.md`
- Development Framework: `uzz/specs/development-framework.md`
- Scope: `uzz/specs/scope.md`
