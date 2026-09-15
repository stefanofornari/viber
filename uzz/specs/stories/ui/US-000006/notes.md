# US-000006 Notes

## Technical Decisions

- Introduced a generic `ste.ai.viber.actor.Actor` interface with `chat(Chat)` and `conversation()` to decouple the CLI from any specific actor implementation.
- `LangChain4jActor` now implements `Actor`, reusing its existing `conversation()` method and `chat(Chat)` behavior without changing production semantics.
- `VibeChatCLI` is a pure REPL: it reads from an injectable `Reader`, writes via a pluggable `Renderer`, and delegates all conversation mutation to the `Actor`. Defaults use `System.in` and `StringRenderer`.
- The CLI renders the conversation before each prompt and after each actor response, so the user always sees the full state.
- Exit is triggered by `/exit` (case-insensitive) or EOF; `IOException` from a closed stream is treated as clean EOF rather than an error.
- Added **picocli** for command-line parsing. `MainCommand` parses `--key`, `--endpoint`, `--model`, `--system-prompt`, and `--echo` into a `CliOptions` mixin.
- When `--echo` is provided, the CLI uses `StdInStdOutActor` (no LLM call). Otherwise it builds an `OpenAiChatModel` from the picocli options and wires it into a `LangChain4jActor`.
- `Main` now delegates to picocli's `CommandLine.execute(args)`, making the entry point fully parameterizable without code changes.
- `InputTool` was fixed to print the prompt text to `System.out` before reading, and to avoid try-with-resources on `System.in` so the stream is not closed after the first read.
- Maven Shade Plugin builds a fat JAR containing all runtime dependencies, with JavaFX and test artifacts excluded.
- No persistence, no session selection UI, no session picker — strictly the interactive REPL described in the acceptance criteria.

## Trade-offs

- `VibeChatCLI` owns the display loop but not the conversation state; the `Actor` owns the `Conversation`. This avoids dual-write inconsistencies but means pre-populating a session requires mutating `actor.conversation()` directly (as shown in the continuation test).
- The `Reader`/`Renderer` injection points make the class testable with System Lambda without resorting to reflection or global `System.in` hacks beyond what the test framework already provides.
- Blank lines are silently skipped rather than treated as prompts, keeping the conversation model clean.
- `StdInStdOutActor` is intentionally minimal ("Echo: ...") because the real interaction path is through `LangChain4jActor` when LLM options are supplied.
