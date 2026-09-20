# Notes: US-000012 — Streaming CLI Rendering

## Technical Decisions

- `ViberCLI` does not own the streaming lifecycle; it merely forwards each `ChatMessage` to `renderConversation()` via the `onMessage` callback.
- The `BufferedReader` loop in `start()` was updated to pass the callback into `actor.chat(...)`. The loop continues to read next prompt after each `chat()` completes.
- `renderConversation()` is idempotent and reads from `actor.conversation()`, so repeated re-renders during streaming are safe.
- `StdInStdOutActor` still emits exactly one `ReplyMessage`, but now through the callback. The CLI test verifies both sync and async actor implementations work through the same callback.

## Trade-offs

- Chose to re-render the entire conversation on each message rather than attempting terminal cursor manipulation. This keeps the CLI simple and platform-independent at the cost of more `System.out` writes.
- The `Reader` injection point remains unchanged, so tests continue to use System Lambda's `withTextFromSystemIn` and `tapSystemOut`.
- No buffering or debouncing of partial messages; every callback invocation triggers a render. This matches the live-update requirement.

## References

- Coding Standard: `uzz/specs/coding-standard.md`
- Development Framework: `uzz/specs/development-framework.md`
- Technical Plan: `.kilo/plans/1789365166439-streaming-actor-plan.md`
