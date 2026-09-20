# Notes: US-000010 — Streaming Actor Contract and LangChain4j Streaming

## Technical Decisions

- Replaced the synchronous `Actor.chat(Chat)` with `void chat(Chat chat, Consumer<ChatMessage> onMessage)`. This is the minimal contract needed for incremental rendering without changing the conversation model.
- `ChatMessage` gained a `default String contentType()` method returning `"text/plain"`. This keeps existing implementations untouched while giving renderers and transports a standard MIME hook.
- `LangChain4jActor` no longer calls `AiServices.chat(...)` returning `Result<String>`. Instead it builds an `AiServices` proxy whose service method returns `TokenStream`, then subscribes to partial responses.
- `StdInStdOutActor` emits exactly one `ReplyMessage` through the callback. It does not attempt streaming because it has no model backing it.
- `ActorService` interface inside `LangChain4jActor` was changed from `Result<String>` to `TokenStream` to match the new LangChain4j streaming API.
- A `CountDownLatch` + `AtomicBoolean` guard prevents duplicate final `ReplyMessage` emission when the model returns no partial chunks.

## Trade-offs

- Chose `Consumer<ChatMessage>` over `Flux<ChatMessage>` or a custom stream type to keep the actor contract dependency-light. Reactor is not on the classpath.
- Tool request/response messages are emitted through `onIntermediateResponse` and `onToolExecuted` rather than parsed from partial text. This matches LangChain4j's structured streaming events.
- `ChatMessage` is used as the common event type. Specific tool-related events were added as new implementations (`ToolExecutionRequestMessage`, `ToolExecutionResponseMessage`).
- Blocking on `CountDownLatch` inside `chat()` preserves the synchronous feel for callers while still enabling incremental callback delivery. A fully async API was deemed out of scope for this story.

## References

- Coding Standard: `uzz/specs/coding-standard.md`
- Development Framework: `uzz/specs/development-framework.md`
- Technical Plan: `.kilo/plans/1789365166439-streaming-actor-plan.md`
