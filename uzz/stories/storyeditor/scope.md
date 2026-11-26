# Domain Name: Viber — UserStoryEditor Control
**Version:** 0.1.0

## Overall Description
`UserStoryEditor` is a reusable JavaFX control to load, edit and save a single user story in the uzz story markdown format (`uzz/skills/kb/references/user_story.md`). It serves two users: a **Developer** editing a story visually, and a **tool** (e.g. a Viber actor) that shows the user a story proposed by an LLM so the user can adjust it before saving.

**Model** — a JavaFX-property-based `UserStory` model (ID, title, role, want, so-that, status, dependencies, technical context, notes, acceptance scenarios). The control exposes it as a property, so host code can bind to it, listen to changes and set it programmatically. A dirty flag tracks unsaved changes.

**Editing** — structured fields: role / want / so-that, a status dropdown (`TODO`, `WIP`, `DONE`), a dependencies list, technical context, notes, and a list of Gherkin scenarios. A raw-markdown tab shows and edits the same story as plain text (no rich-text editing); the two views stay in sync. The raw tab has a **Preview** button that renders the current markdown, using the markdown preview component already in the project.

**Loading an LLM proposal** — a proposed story is loaded as a plain current story (no diff, no per-field accept/reject); the user edits and saves as for any other story.

**Persistence** — load from and save to a markdown file or string; saving a story and loading it back yields an equal model. Invalid or unparseable markdown is reported to the user and does not replace the current story.

**UI and styling** — layout defined in FXML with a controller; styled with AtlantaFX (already a project dependency). The feature is isolated in its own Java package. There is no separate demo application: the control is exercised in the existing dev mode window of the Manual Actor (`FXActor`, `DevModeWindow`), where it is added as an additional view. It is not wired into the Viber chat flow, other actors or the Story phase in this version.

**Decisions taken** — lives in the current Maven module and own package; structured editing plus raw-markdown tab with preview; plain-edit semantics for LLM proposals; no new dependencies; hosted in the Manual Actor dev mode window.

**Note** — a WYSIWYG markdown editor (e.g. [Milkdown](https://github.com/Milkdown/milkdown)) may be considered in the future to replace the plain-text tab; not part of this scope.

## In-Scope Capabilities
* **Model**:
  - [ ] `UserStory` model with JavaFX properties for all story fields
  - [ ] Parse a story from uzz markdown and render it back to markdown (round-trip safe)
  - [ ] Dirty (modified) state tracking, reset on load/save
* **Control API**:
  - [ ] `UserStoryEditor` control embeddable in any JavaFX scene
  - [ ] `story` property to get, set and observe the edited story
  - [ ] `load(Path)` / `load(String)` and `save(Path)` / `toMarkdown()` operations
  - [ ] Read-only mode toggle
  - [ ] Notification of load, save and validation errors to the host
* **UI (FXML)**:
  - [ ] Structured fields for ID, title, role / want / so-that
  - [ ] Status dropdown
  - [ ] Add / remove / edit dependencies
  - [ ] Add / remove / reorder / edit Gherkin scenarios
  - [ ] Technical context and notes text areas
  - [ ] Raw-markdown plain-text tab kept in sync with the structured view
  - [ ] Preview button rendering the markdown
  - [ ] Visible unsaved-changes indication
* **Styling**:
  - [ ] AtlantaFX theme applied to the control
* **Integration**:
  - [ ] Control available in the Manual Actor dev mode window to open, edit and save a story file, and to load a sample "LLM-proposed" story

## Out-of-Scope / Future Enhancements
* Rich-text / WYSIWYG markdown editing (Milkdown may be considered later), syntax highlighting
* Diff / per-field accept-reject view of LLM proposals
* Wiring into the Viber chat flow, actors or the Story phase
* Separate demo application
* Constraining the dependency input to existing user stories (deferred, US-000076)
* Editing multiple stories, story index (`index.md`) maintenance, ID generation
* Automated validation against INVEST or Gherkin semantics (only structural parsing)
* Undo/redo, spell checking, localization
* Separate Maven module or artifact
* NetBeans integration (`ViberNB`)

## Technical Context & Infrastructure
* **Development Toolchain**: Java, JavaFX (FXML), AtlantaFX, javafx-markdown-preview, Maven, JUnit 5, TestFX, AssertJ (BDD `then()`), headless tests, TDD
* **Target Systems/Platforms**: JavaFX desktop applications; host applications embedding the control
* **Project Reference Docs**:
  - Coding Standard: `uzz/project/coding_standard.md`
  - Development Framework: `uzz/project/development_framework.md`
  - User Story Standard: `uzz/skills/kb/references/user_story.md`

### Keywords
* user-story-editor
* javafx-control
* fxml
* atlantafx
* javafx-properties
* markdown-preview
* llm-proposal
