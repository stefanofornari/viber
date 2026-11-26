# Notes for US-000066

- Simplified from a list editor to one `TextField` (`dependenciesField`) with comma-separated IDs. Entries are trimmed; blanks and duplicates dropped.
- Text to model: parsed on every change. Model to text: rewritten only if the parsed current text differs from the list, so typing a trailing comma or spaces is not clobbered.
- **Equality pitfall:** `UserStory` equality is content-based, and JavaFX change listeners skip events where old and new values are equal. The editor therefore uses an invalidation listener plus its own `bound` reference; otherwise setting an equal-content story would leave the UI bound to the old one. Hosts observing `storyProperty()` should use an `InvalidationListener` for the same reason.
