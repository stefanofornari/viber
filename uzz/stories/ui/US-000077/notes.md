# US-000077 — Implementation notes

1. The ring is 18x18 like the archive button (not the 32x32 of the sample), with a 6px gap before the label (an HBox margin, applied only while the ring is managed).
2. `GUIRenderer.communicating(chat, active)` is the entry point; `MainAppController.handleSend` turns it on before starting the chat thread and off in a `finally`.
3. The activity state is kept per chat (identity set) in `ConversationPaneController`, because `FXActor` re-renders the whole conversation, rebuilding all tabs; the ring is restored from that set.
4. Tabs without archive/forget buttons use the ring as the tab graphic, so it still precedes the text label.
5. `FXActor.chat` returns immediately and the answer comes later from the dev mode window, so for the manual actor the ring stays on until `FXActor` adds a reply or an error (`communicationEndHandler`); for the other actors it ends when `chat()` returns.
