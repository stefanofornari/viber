# LangChain4j Reasoning / Thinking Streaming API

## Overview
LangChain4j exposes reasoning/thinking tokens from compatible LLMs through dedicated streaming callbacks. These are separate from regular text response tokens.

## Key Types

- `dev.langchain4j.model.chat.response.PartialThinking` — represents a partial thinking/reasoning chunk, usually a single token. Available since 1.2.0.
- `dev.langchain4j.model.chat.response.PartialThinkingContext` — provides a `StreamingHandle` for cancellation. Available since 1.8.0.

## TokenStream Callbacks

`TokenStream` exposes the following reasoning-related callbacks:

```java
TokenStream stream = service.chat(prompt);

stream.onPartialThinking(partialThinking -> {
    // reasoning text, usually a single token
    String text = partialThinking.text();
});

stream.onPartialThinkingWithContext((partialThinking, context) -> {
    String text = partialThinking.text();
    context.streamingHandle().isCancelled(); // can cancel
});

stream.start();
```

## StreamingChatResponseHandler Callbacks

When using `StreamingChatResponseHandler` directly:

```java
model.chat(userMessage, new StreamingChatResponseHandler() {
    @Override
    public void onPartialThinking(PartialThinking partialThinking) {
        System.out.println("thinking: " + partialThinking.text());
    }

    @Override
    public void onPartialThinking(PartialThinking partialThinking, PartialThinkingContext context) {
        // with cancellation handle
    }
});
```

## Notes
- Not all models produce thinking tokens. Models like Claude with extended thinking or DeepSeek R1 emit them.
- Thinking tokens may arrive in batches, not strictly one token at a time.
- If no thinking is emitted, the `onPartialThinking` callbacks are simply not invoked.
