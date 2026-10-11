# US-000001 Notes

## Implementation Notes

- `Conversation`, `Chat`, and message types implemented as immutable/thread-safe structures
- `Conversation` uses `CopyOnWriteArrayList` for chat storage
- `Chat` stores messages in insertion order
- `SystemMessage` is optional and set via `Conversation.systemMessage()`

## Developer Documentation

- See `docs/development.md` for a developer-focused guide covering this and related completed user stories in the model and development domains.
