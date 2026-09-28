# US-000005 Notes

## Implementation Notes

- `JavaFxRenderer` extends `ScrollPane` and implements `Renderer`
- Loads UI from `ConversationPane.fxml` using `FXMLLoader.setRoot(this)`
- Never creates or manages a `Stage`; host application is responsible for embedding
- All render methods marshal updates to the JavaFX Application Thread via `Platform.runLater()`

## Developer Documentation

- See `docs/development.md` for a developer-focused guide covering this and related completed user stories in the model and development domains.
