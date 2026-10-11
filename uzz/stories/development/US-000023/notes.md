# US-000023 Notes

## Technical Decisions

- **GUIRenderer as JavaFX Node**: Changed `GUIRenderer` from a plain Java class to extend `ScrollPane`, making it a first-class JavaFX node that can be embedded directly in FXML. `getRoot()` now returns `this` for backward compatibility.

- **FXML `fx:root` pattern**: Updated `ConversationPane.fxml` to use `<fx:root type="javafx.scene.control.ScrollPane">` instead of `<ScrollPane>`, enabling `FXMLLoader.setRoot(this)` to merge FXML properties into the `GUIRenderer` instance.

- **FXML injection in MainAppWindow**: Removed programmatic `root.getChildren().add(1, renderer.getRoot())` from `MainAppController.initialize()`. The renderer is now declared in `MainAppWindow.fxml` as `<GUIRenderer fx:id="conversation" />` and injected via `@FXML`.

## Implementation Changes

- `GUIRenderer.java`: extends `ScrollPane`, loads `ConversationPane.fxml` with `setRoot(this)`, attaches stylesheet programmatically
- `ConversationPane.fxml`: switched to `<fx:root>` pattern
- `MainAppWindow.fxml`: added `<GUIRenderer fx:id="conversation" />`
- `MainAppController.java`: uses FXML-injected `conversation` field; removed `renderer` parameter from `initialize()`
- `MainAppWindow.java`: removed `renderer` parameter and `getRenderer()` method
- `ViberApplication.java`: updated `MainAppWindow` instantiation
- `CLIOptions.java`: removed `--dev` and `--fx-render` flags; `--gui` now implies standalone GUI mode with embedded dev tools
- `ViberApplication.createActor()`: returns `FXActor` when `--gui` is enabled or no `--key` is provided
- `FXActor.java`: renderer is no longer `final`; added `renderer(Renderer)` for late binding
- `MainAppController.initialize()`: accepts `Actor` and `Stage owner`; defers `DevModeWindow` creation until owner stage is showing via `owner.showingProperty()` listener, ensuring correct positioning next to the main window
- `MainAppController.switchToFXActor()`: reopens `DevModeWindow` when switching back from LLM, only if owner is showing
- `MainAppController.switchToLangChain4jActor()`: hides and clears `DevModeWindow` when switching to LLM
- `DevModeWindow.java`: accepts `Stage owner` and positions itself next to the main window; both windows are centered on screen

## Test Changes

- `MainAppControllerTest`: removed `GUIRenderer` parameter from `loadController()` helpers; added `fxml_injects_conversation_renderer` and `fxml_renderer_displays_conversation` tests; uses anonymous `Actor` stub and `AtomicReference<Stage>` via `interact()` to satisfy FX-thread requirements
- `MainCommandTest`: removed `--dev` and `--fx-render` related tests
- Added `TestNameLogger` extension to print test names before execution

## Validation

- Full test suite passes: 139 tests, 0 failures, 0 errors

## Developer Documentation

- See `docs/development.md` for a developer-focused guide covering this and related completed user stories in the model and development domains.
