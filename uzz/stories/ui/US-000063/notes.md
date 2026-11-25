# Notes — US-000063

- Project logic lives in `ProjectInfo` (cli package), separate from the controller; the controller only renders it via `statusLine(ProjectInfo)`, which tests can call directly with a temp directory.
- Heading detection: a line starting with `# ` (hash + space) whose stripped text is not empty; blank `# ` headings are skipped, so a later non-blank heading still wins. `#NoSpace` and `##` lines are not level-1 headings.
- Any `IOException` while reading `overview.md` (missing, a directory, invalid UTF-8) falls back to the folder name, silently.
- Middle truncation uses the Label's `textOverrun="CENTER_ELLIPSIS"`; the full path is in a tooltip on the same label.
- The working directory is read from `user.dir` once, in `MainAppController.initialize`; the actors keep resolving `user.dir` on their own, untouched.

## Verification status
- Written in a sandbox that cannot reach Maven Central: `ProjectInfo` was compiled and exercised standalone (heading, fallback, directory-as-file, root), but the Maven build and the JUnit/TestFX tests (`ProjectInfoTest`, new tests in `MainAppControllerTest`) have not been run. Story left at WIP until the suite passes.
