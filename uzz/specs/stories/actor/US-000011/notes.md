# US-000011 Notes

## Findings

- LangChain4j exposes reasoning tokens via `TokenStream.onPartialThinking(Consumer<PartialThinking>)` and `onPartialThinkingWithContext(BiConsumer<PartialThinking, PartialThinkingContext>)`.
- `PartialThinking.text()` returns the reasoning text chunk.
- `PartialThinking` is available since 1.2.0; `PartialThinkingContext` since 1.8.0. Project uses 1.18.1, so both are available.
- Models that support reasoning: Claude with extended thinking, DeepSeek R1, etc. Not all models emit thinking tokens.
- Thinking tokens may arrive in batches, not strictly one token at a time.

## Decisions

- Mirror the newline-buffering strategy used for `ReplyMessage` in `onPartialResponse`.
- Emit `ThoughtMessage` only when a newline boundary is reached in the buffered thinking text.
- Flush remaining buffered thinking text in `onCompleteResponse` if it doesn't end with a newline.
- Tool execution callbacks remain immediate and unchanged.

## Implementation Summary

- `LangChain4jActor.chat()` now registers `onPartialThinking` on the `TokenStream`.
- A `StringBuilder thinkingBuffer` accumulates partial thinking text.
- On each newline in the buffer, a `ThoughtMessage` is created, appended to the chat, emitted via `onMessage.accept()`, and `update()` is called.
- In `onCompleteResponse`, any remaining buffered thinking text is flushed as a final `ThoughtMessage`.
- Non-reasoning models are unaffected: the `onPartialThinking` callback is simply not invoked.
