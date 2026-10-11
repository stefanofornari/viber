# US-000016 Notes

## Implementation Notes

- Dev mode window opened via `DevModeWindow` when application starts with `--gui`
- Uses `DevModeController` to handle button actions
- Window is positioned adjacent to the main application window, both centered on screen
- Opens only after the main window is showing via `owner.showingProperty()` listener

## Developer Documentation

- See `docs/development.md` for a developer-focused guide covering this and related completed user stories in the model and development domains.
