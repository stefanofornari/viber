# Feature: Viber Chat UI — Scrolling, Radial Phase Selector, Chat Tab Lifecycle, and Status Line

## Overall Description

Improvements to Viber's JavaFX chat interface.

**Scrolling** — enable scrolling in Viber's shared JavaFX chat renderer, used by both the standalone application and host applications embedding it. Mouse-wheel input scrolls content under the pointer first; when nested message content has reached its limit, a later wheel event can scroll the outer chat. Keyboard input scrolls the focused element.

**Radial phase selector** — the standalone ViberFX main window has a permanently visible radial control immediately to the left of the prompt input. The control exposes the workflow modes Scope, Story, Code, and Explore as radial wedges around a central hub, providing a direct alternative to selecting actors from the Actor menu. The radial is inline with the prompt `TextArea`, its overall diameter matches the input area height, and it occupies a fixed circular slot that never widens the input area. The central hub shows the active mode and a neutral indicator when a non-radial actor (Manual or LLM) is active. Selecting a wedge has exact behavioral parity with the corresponding menu action.

**Chat tab lifecycle** — each chat tab title has separate small icon buttons for closing and archiving the conversation or closing and forgetting it. Archiving retains the complete conversation for future resumption. Forgetting requires confirmation and permanently discards the conversation. Finding and resuming archived conversations is outside this feature.

**Status line** — the standalone ViberFX main window has a read-only status line at the bottom, below the conversation and prompt area, showing the project name and the project working directory. The project name is the first level-1 heading of `uzz/project/overview.md` (resolved against the working directory); when the file is missing, unreadable or has no such heading, the name of the working directory's folder is shown instead. The working directory is the JVM current working directory (`user.dir`), the same directory the actors' file tools are rooted at. The full absolute path is shown, truncated in the middle when it does not fit, with the full path in a tooltip. Both values are read once at startup.

## In-Scope Capabilities

* **Chat Scrolling**:
  - [ ] Vertical wheel input over the chat scrolls the chat content without requiring scrollbar operation.
  - [ ] Horizontal wheel input scrolls horizontally only when the relevant chat or message content has horizontal overflow; otherwise, it is ignored.
  - [ ] When the pointer is over scrollable content inside a message, wheel input scrolls that content first.
  - [ ] If nested message content is at its limit, it does not pass that wheel event to the outer chat. A subsequent wheel event scrolls the outer chat if it can still scroll in that direction.
  - [ ] Arrow keys scroll the focused element in the corresponding direction when it can scroll that way.
  - [ ] Page Up and Page Down scroll the focused element vertically when it can scroll.
  - [ ] Apply the behavior in the shared JavaFX chat renderer, including embedded use.

* **Radial Phase Selector**:
  - [ ] Render a permanently visible radial control immediately left of the prompt `TextArea` in the standalone ViberFX main window.
  - [ ] Constrain the whole radial control (diameter) to the current height of the input area.
  - [ ] Expose exactly four wedges: Scope, Story, Code, and Explore.
  - [ ] Map Scope to the existing `switchToScopeActor()` behavior, Story to `switchToStoryActor()`, Code to `switchToCodingActor()`, and Explore to the same behavior as the Actor menu's LLM option (`switchToLangChain4jActor()`).
  - [ ] Treat Explore as a label/alias for the existing LLM actor; do not introduce a separate ExploreActor implementation.
  - [ ] Keep all wedges visible at all times; there is no collapse/expand trigger.
  - [ ] Implement the control with `fx-radial-menu` (`RadialMenu`, `MenuItem`, selection callback) per `uzz/library/fx-radial-menu.md`.
  - [ ] Show the active mode in the central hub: Scope, Story, Code, or Explore; show a neutral indicator when the active actor was selected from the Actor menu as Manual or LLM.
  - [ ] Highlight the active wedge when Scope, Story, Code, or Explore was selected from the radial; clear the radial highlight when the actor is switched from the Actor menu.
  - [ ] Keep radial state synchronized when the actor is switched through the existing Actor menu.
  - [ ] Do not show a Scope/Story/Code phase indicator when Explore is active.

* **Inline Layout**:
  - [ ] Place the radial control in the same horizontal row as the prompt `TextArea`, with the radial on the left.
  - [ ] Give the radial a fixed circular footprint; hovering or selecting wedges must not change its width or the row layout.
  - [ ] Let the prompt `TextArea` keep all remaining horizontal space; the radial must not expand the input area width.
  - [ ] Track input area height changes, including when the prompt grows and when a `QuestionsPane` is inserted and the prompt is constrained to `ASK_TOOL_PROMPT_HEIGHT`.
  - [ ] Remain circular and square as its height tracks the input area.
  - [ ] Avoid overlapping the `scopePhaseIndicator` label below the prompt.

* **Selection and State Parity**:
  - [ ] Selecting a radial wedge performs the same immediate actor switch as the corresponding Actor menu action.
  - [ ] Preserve existing API-key validation, failure handling, and error messages for LLM, Explore, and phase initialization.
  - [ ] Preserve existing `scopePhaseIndicator` text and styling behavior after a radial selection.
  - [ ] Leave the existing Actor menu unchanged, including Manual, LLM, Scope, Story, and Coding entries.

* **Chat Tab Lifecycle**:
  - [ ] Each chat tab title contains separate small icon buttons for “Close and archive” and “Close and forget,” using the existing AtlantaFX framework and icon set.
  - [ ] “Close and archive” closes the tab and retains the complete conversation in storage so it can be fully resumed by a future feature.
  - [ ] “Close and forget” displays a confirmation warning that the conversation will be completely discarded.
  - [ ] Declining or dismissing the confirmation leaves the tab and conversation unchanged.
  - [ ] Confirming “Close and forget” closes the tab and discards an unarchived conversation from memory.
  - [ ] Confirming “Close and forget” for a previously archived conversation closes the tab and discards it from storage.

* **Status Line**:
  - [ ] Render a read-only status line at the bottom of the standalone ViberFX main window (`MainAppWindow`), below the conversation/input `SplitPane`, spanning the window width.
  - [ ] Show the project name and the working directory in the status line.
  - [ ] Take the project name from the first level-1 heading (`# `) of `uzz/project/overview.md`, resolved against the working directory.
  - [ ] Fall back to the name of the working directory's folder when `uzz/project/overview.md` is missing, unreadable, or has no non-blank level-1 heading.
  - [ ] Use the JVM current working directory (`user.dir`) as the working directory, consistent with the directory used by the actors' file tools.
  - [ ] Show the full absolute path of the working directory, truncated in the middle when it does not fit the available width, with the full path in a tooltip.
  - [ ] Read both values once at startup; the status line does not refresh while the application is running.
  - [ ] A missing or unreadable `overview.md` is not an error: no dialog, no exception, and the main window still opens.
  - [ ] Leave the layout of the `SplitPane`, prompt row, radial slot and `scopePhaseIndicator` unchanged.

* **Integration**:
  - [ ] Add the `com.github.stefanofornari:fx-radial-menu:1.0-SNAPSHOT` Maven dependency.
  - [ ] Apply the radial menu AtlantaFX integration stylesheet as documented, after the application theme, if required for visual consistency.
  - [ ] Keep UI construction separate from business logic according to `uzz/project/coding_standard.md`.

## Out-of-Scope / Future Enhancements

* Keyboard scrolling outside the chat renderer.
* Changes to scrollbar appearance or behavior beyond supporting wheel scrolling.
* Automatic scrolling to the newest message or kinetic/inertial scrolling.
* Scrolling behavior changes to the CLI or other non-JavaFX interfaces.
* Adding the radial phase selector to the embeddable JavaFX component, the Dev Mode conversation editor, `ViberNB`, or `ViberCLI`.
* Adding Explore to the Actor menu, or removing/replacing any Actor menu item.
* Exposing Manual as a radial wedge, or exposing LLM and Explore as separate wedges.
* Implementing an independent `ExploreActor` or changing LLM actor behavior.
* New actor selection behavior, workflow orchestration, or automatic phase transitions.
* Changes to conversation history handling, phase completion detection, or phase indicator semantics.
* Custom animations, tooltips, keyboard accelerators, or accessibility enhancements beyond `fx-radial-menu` defaults.
* User-resizable radial size beyond tracking the input area height.
* Persisting the active mode across application restarts.
* Providing an interface to discover, browse, reopen, or resume archived conversations.
* Restoring a conversation after it has been forgotten.
* Adding the status line to the embeddable JavaFX component, the Dev Mode window, `ViberNB`, or `ViberCLI`.
* Creating, seeding or editing `uzz/project/overview.md` (neither by the application nor from the status line).
* Refreshing or watching the status line while the application is running (project or file changes, changing directory).
* Making the project name or working directory selectable, clickable or editable (copy path, open folder, switch project).
* Showing further information in the status line (version, Git branch, active actor or model, connection state).
* Reading any other `overview.md` field (version, group/artifact ID) or other formats (front matter, non-`#` first lines).
* Changing how the actors resolve their working directory, or introducing a configurable working directory.

## Technical Context & Infrastructure

* **Development Toolchain**: Java 21, JavaFX 23.0.2, CommonsFX, AtlantaFX, Maven, JUnit 5, TestFX, AssertJ, TDD-oriented workflow
* **Target Systems/Platforms**: Viber JavaFX desktop UI (chat tab lifecycle and radial phase selector); shared JavaFX chat renderer including host applications embedding it (scrolling)
* **Project Reference Docs**:
  - Coding Standard: `uzz/project/coding_standard.md`
  - Development Framework: `uzz/project/development_framework.md`
  - Radial Menu Component: `uzz/library/fx-radial-menu.md`

### Keywords
- javafx
- gui
- scrolling
- mouse-wheel
- keyboard
- radial-menu
- phase-selector
- scope-story-code
- explore
- actor-selection
- chat-tabs
- archive
- forget
- viberfx
- status-line
- project-name
- working-directory
- overview
