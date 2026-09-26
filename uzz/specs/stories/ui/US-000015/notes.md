# Notes: US-000015 — CLI-triggered JavaFX Conversation Rendering with AtlantaFX Styles

## Technical Decisions

- **ViberFxWindow as Application entry point**: A dedicated `Application` subclass (`ViberFxWindow`) initializes the JavaFX runtime and sets the AtlantaFX user agent stylesheet before any scene is created.
- **AtlantaFX NordLight theme**: The project already depends on `atlantafx-base` and uses `NordLight` in other modules. `Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet())` is called in `start()` before rendering the conversation.
- **Static show(Conversation) entry point**: `ViberFxWindow.show(Conversation)` stores the conversation in a static field and launches the Application in a background thread, mirroring how `LogViewerWindow` is launched from `MainCommand`.
- **Reuse of JavaFxRenderer**: The window delegates to the existing `JavaFxRenderer` to build the scene graph from FXML, keeping rendering logic in one place.
- **CLI wiring**: `CliOptions` gains a `--fx-render` boolean flag. `MainCommand.call()` launches `ViberFxWindow` before starting `ViberCLI`, so the FX window is ready when the first render occurs.

## Trade-offs

- `Application.launch()` can only be called once per JVM. If both `--show-log` and `--fx-render` are specified, only the first launched Application will succeed. This is a known limitation acceptable for the MVP; a combined shell Application could be introduced later if both windows are needed simultaneously.
- The conversation reference is passed via a static field to bridge the gap between the CLI thread and the JavaFX Application thread. This is safe because `JavaFxRenderer.render()` schedules UI work on the FX thread via `Platform.runLater`.

## References

- Coding Standard: `uzz/specs/coding-standard.md`
- Development Framework: `uzz/specs/development-framework.md`
- Scope: `uzz/specs/scope.md`
