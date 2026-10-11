## Interactive mode and tools execution policy
The basic idea is that a tool is classified based on its effect on the system as `READONLY`/`READWRITE`/`INTERACTIVE`/`UNKNOWN`; then, depending on how we are interacting with the UI, Jeddict executes the tool directly, previa confirmation by the user, or interactively together with the user. This depends on the **InteractionMode** dropdown:

1. ASK - the chat is for questions and responses only, no tool is executed regardless
2. AGENT - like today, the agent has full control, any tool is directly executed
3. INTERACTIVE - the following policy applies:
  3.1. tools with `@ToolPolicy(READONLY)`  and `@ToolPolicy(INTERACTIVE)` are executed right away
  3.2. tools with `@ToolPolicy(READWRITE)`, `@ToolPolicy(UNKNOWN)` or no `@ToolPolicy` annotation are executed only after user consent
  3.3. note that interactive tools are supposed to have a UI and the interaction with the user implies user consent to apply the result of the interaction

### Main changes:
- **`@ToolPolicy` Annotation**: A new annotation to classify tools based on their potential impact.
   - `READONLY`: For safe, read-only operations.
   - `READWRITE`: For operations that modify files or system state.
   - `INTERACTIVE`: For tools that require user interaction.
   - `UNKNOWN`: The default for any unclassified tool.
- **`HumanInTheMiddleWrapper`**: A wrapper class built with Byte Buddy that creates a dynamic proxy around any tool object.
     - For methods annotated with `@Tool`, it inspects the `@ToolPolicy`.
     - If the policy is `READWRITE` or `UNKNOWN` (or if no policy is specified), it invokes a configurable _Human In The Middle_ function. This function receives a formatted string describing the tool call (method name and arguments) and returns a `boolean` to approve or deny the execution.
     - When in INTERACTIVE mode, if a tool requires a confirmation popup, an option pane pops up showing the tool that would like to execute and its parameters. If the user clicks YES the function is executed, if the user clicks NO, the tool is not executed. It is up to the model to decide what to do, but we probably need to improve the prompt:
<img width="1294" height="215" alt="image" src="https://github.com/user-attachments/assets/49d391f8-110e-43a8-ae9c-06a0dc7d4dba" />

### Proxy Implementation
- The proxy created by `HumanInTheMiddleWrapper` is a true delegate. It forwards **all** method calls to the original tool instance preserving their annotations so that langchain4j still detects the proxies as tools.
