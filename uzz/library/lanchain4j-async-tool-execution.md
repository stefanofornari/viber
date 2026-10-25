# LangChain4j Tool Execution Flow

LangChain4j handles sending tool execution results back to the LLM differently depending on whether you are using the high-level **AI Services** abstraction or the low-level **StreamingChatModel** directly.

---

## Scenario 1: Using High-Level AI Services (`@Tool` methods with `TokenStream` return type)

If you are using `AiServices.builder().tools(...)` and your AI Service method returns a `TokenStream` (as in the example with `onToolExecuted`), LangChain4j automatically orchestrates sending the tool execution results back to the LLM.

1. **User sends a prompt.**
2. **LangChain4j sends it to the LLM.**
3. **LLM returns tool calls.** The `TokenStream` will receive these tool calls.
4. **Tools execute concurrently.** If you have configured your AI Service with `executeToolsConcurrently()`, LangChain4j will execute the multiple tool calls in separate threads as soon as their `onCompleteToolCall` event is internally received, without waiting for other tools or the response streaming to complete.
5. **LangChain4j sends results back to the LLM.** Once these tool executions are complete, LangChain4j internally creates `ToolExecutionResultMessage` objects from the results and automatically sends them as part of a subsequent `ChatRequest` to the LLM. This happens as the next "turn" in the conversation with the LLM. You do not need to manually construct or send this `ChatRequest`.
6. **LLM generates final response.** Based on the tool results provided by LangChain4j, the LLM continues the conversation and generates its final textual response.
7. **TokenStream emits final response.** The final response from the LLM is then streamed back to your application via `onPartialResponse` and `onCompleteResponse` callbacks.

> **Note:** The `onToolExecuted` callback in this scenario is primarily for observing when a tool has completed execution and what its result was; it's not where you manually send the results back to the LLM.

---

## Scenario 2: Using Low-Level `StreamingChatModel` directly with `StreamingChatResponseHandler`

If you are working directly with `StreamingChatModel` and providing a `StreamingChatResponseHandler` (where you might observe `onPartialToolCall`), you are responsible for manually sending the results back to the LLM.

1. **User sends a prompt.** You create a `ChatRequest` and call `model.chat(request, new StreamingChatResponseHandler() { ... })`.
2. **LLM returns tool calls.** Your `onCompleteToolCall(CompleteToolCall completeToolCall)` callback will be invoked for each tool the LLM wants to call.
3. **You execute the tools.** You need to implement your own logic to:
   - Collect all `CompleteToolCall` instances.
   - Execute these tools using their name and arguments (you would typically run them concurrently in your own `Executor` if multiple tools are present).
4. **You send results back to the LLM.** After you have executed all the tools and collected their results, you must:
   - Create `ToolExecutionResultMessage` objects for each tool execution result.
   - Construct a new `ChatRequest` that includes:
     - The original `UserMessage`.
     - The `AiMessage` that contained the `ToolExecutionRequests` (received from the LLM in step 2).
     - All the `ToolExecutionResultMessages` you just created.
   - Make another call to `model.chat(request2, new StreamingChatResponseHandler() { ... })` to send these results back to the LLM.
5. **LLM generates final response.** The LLM will then process the tool results and generate its final response, which will be streamed back to your `StreamingChatResponseHandler`.

---

## Summary

* **High-Level AI Services:** LangChain4j handles the entire multi-turn interaction including sending tool results back to the LLM automatically.
* **Low-Level `StreamingChatModel`:** You have explicit control and must manually manage the round-trip by sending a new `ChatRequest` containing the tool execution results.