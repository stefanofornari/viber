# US-000062 Implementation Notes

- Per the persistence limitation carried over from US-000061, this iteration implements the confirmation UI and a forget hook, not actual in-memory or stored deletion.
- The standalone app displays “persistence not yet implemented” after confirmation and leaves the tab open.
- `GUIRenderer.forgetHandler` accepts a handler for a `Chat`; returning `true` indicates that it was discarded and allows the renderer to close that tab.
- Chat titles and their archive/forget controls share a tab graphic so the title precedes the controls; icon buttons have a fixed 18px width.
- Keep US-000062 in WIP until the handler removes unarchived chats from memory and archived chats from persistent storage.
