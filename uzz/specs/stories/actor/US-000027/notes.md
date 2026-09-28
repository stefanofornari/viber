# US-000027 Notes

## Implementation Notes

- Made `AbstractTool` implement `Tool` so `FileSystemTools` can be passed to `LangChain4jActor` as a `List<Tool>`.
- Wired `FileSystemTools` into `ViberApplication.createActor()` with `System.getProperty("user.dir")` as the default basedir.
- Added `FileSystemToolsTest` covering read, create, delete, list, and basedir confinement.
- Added `LangChain4jActorTest#chat_with_filesystem_tools_creates_actor_successfully` to verify actor construction with filesystem tools.

## Developer Documentation

- See `docs/development.md` for a developer-focused guide covering this and related completed user stories in the model and development domains.
