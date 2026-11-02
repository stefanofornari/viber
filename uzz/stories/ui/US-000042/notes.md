# Notes

- Route wheel events from the chat pane so the nearest descendant `ScrollPane` that can scroll in that direction handles the event. Consume each event to avoid native JavaFX handling it a second time; do not chain one event from nested content to the outer chat.
- Handle horizontal wheel deltas only when the selected pane has horizontal overflow; do not reinterpret an unavailable horizontal movement as vertical.
- Scale wheel deltas by the selected pane's content overflow, clamping the scroll position to its configured minimum and maximum.
- Keep the change in the shared JavaFX chat pane so it applies to standalone and embedded rendering.
