# Notes: US-000003 — Text-based Conversation Rendering

## Technical Decisions

- **Renderer as consumer**: `Renderer` is an interface with a single side-effecting method `render(Conversation)`. The renderer consumes the conversation and produces output internally — it does not return a value.
- **StringRenderer as output holder**: `StringRenderer` implements `Renderer` and accumulates output in an internal `StringBuilder`. The rendered result is retrieved via `toString()`.
- **No static methods**: No static factory methods, no static utility methods. `StringRenderer` is instantiated directly with `new StringRenderer()`.
- **Type-specific prefixes**: Each `MessageType` gets a distinct visual prefix so messages are recognizable at a glance without relying solely on the type label.

## Defined Text Format

The plain-text renderer produces a deterministic structure:

```
--- Chat N ---
  ROLE/TYPEPREFIXcontent

--- Chat N+1 ---
  ROLE/TYPEPREFIXcontent
```

Type prefixes:
- `PROMPT` → `> `
- `REPLY` → `: `
- `THOUGHT` → `~ `
- `TOOL_EXECUTION_REQUEST` → `[TOOL] `
- `TOOL_EXECUTION_RESPONSE` → `[OUT] `

Rules:
- Each chat is prefixed with `--- Chat N ---` on its own line, 1-indexed.
- Each message is indented by two spaces, followed by `ROLE/TYPE` then the type-specific prefix, then content.
- A blank line separates consecutive chats.
- An empty conversation renders as `(empty conversation)` followed by a newline.

## Trade-offs

- `Chat` and `Conversation` are not full immutable records because they need ordered collection mutation during construction. The unmodifiable-view pattern was chosen over a builder to keep the API simple for this slice.
- `System.lineSeparator()` is used for line breaks to be platform-independent. Output may vary by OS.
- `StringRenderer` is package-private. Consumers instantiate it directly. No factory or registry is introduced at this stage.

## References

- Coding Standard: `uzz/specs/coding-standard.md`
- Development Framework: `uzz/specs/development-framework.md`
