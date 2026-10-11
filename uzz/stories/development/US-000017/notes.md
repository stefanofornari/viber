# US-000017 Notes

## Implementation Notes

- `--gui` flag added to `CLIOptions` using PicoCLI
- Replaces removed `--dev` and `--fx-render` flags
- Does not interfere with existing flags (`--show-log`, `--echo`, `--key`, `--endpoint`, `--model`, `--system-prompt`)
- When `--gui` is enabled and no `--key` is provided, `FXActor` is used as the default actor

## Developer Documentation

- See `docs/development.md` for a developer-focused guide covering this and related completed user stories in the model and development domains.
