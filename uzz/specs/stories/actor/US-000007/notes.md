# US-000007 Notes

## Technical Decisions

- Introduced a `Viber` interface with a single `start()` method to represent a goal-oriented conversation session.
- Added `HelloWorldViber` as the first concrete implementation, now backed by a `LangChain4jActor`.
- Created `LangChain4jActor` to wrap LangChain4j's `AiServices`. It owns the ACTOR role, receives a `ChatModel`, tools, a system prompt, and a `ConversationUpdateListener` callback. It exposes `chat(String)` and does not return a value; conversation updates are pushed through the callback.
- Created generic `InputTool` in `ste.ai.viber.tool` with an `input(String prompt)` method. It reads one line from stdin and returns it, with a fallback of `"(no name provided)"` on EOF/error.
- Made `StringRenderer` public so production code outside the renderer package can reuse the existing text-rendering behavior.
- Kept `HelloWorldViber` free of UI concerns: it constructs a `Conversation` model and delegates rendering to `StringRenderer`.
- Replaced manual `System.in`/`System.out` redirection in tests with **System Lambda** (`tapSystemOut`, `withTextFromSystemIn`) to keep the test clean and aligned with JUnit 5.
- `HelloWorldViber` receives a `ChatModel` via constructor. In tests this is a configured `DummyChatModel`; in production it should be wired through a factory or dependency injection.

## Trade-offs

- `DummyChatModel` was extended to support tool arguments via a new prompt format: `execute tool <name> with arguments:\n<args>`. This keeps test scenarios readable without changing the production tool API.
- `LangChain4jActor` uses a top-level `ActorService` interface with `Result<String> chat(@UserMessage String userMessage)` so LangChain4j's native tool-execution loop and result metadata are available.
- `SystemMessage` is now modeled in the Viber domain and stored in `Conversation`. `LangChain4jActor` sets it from the provided system prompt.
- `StringRenderer` renders the system message before chats, using the format `--- System ---` followed by the content.
- `Main` no longer references `DummyChatModel`; it currently throws `UnsupportedOperationException` because a real `ChatModel` provider has not been wired yet.
