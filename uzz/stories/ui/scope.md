# Feature: Viber Chat Window Scrolling

## Overall Description
Enable scrolling in Viber's shared JavaFX chat renderer, used by both the standalone application and host applications embedding it. Mouse-wheel input scrolls content under the pointer first; when nested message content has reached its limit, a later wheel event can scroll the outer chat. Keyboard input scrolls the focused element.

## In-Scope Capabilities
* **Chat Scrolling**:
  - [ ] Vertical wheel input over the chat scrolls the chat content without requiring scrollbar operation.
  - [ ] Horizontal wheel input scrolls horizontally only when the relevant chat or message content has horizontal overflow; otherwise, it is ignored.
  - [ ] When the pointer is over scrollable content inside a message, wheel input scrolls that content first.
  - [ ] If nested message content is at its limit, it does not pass that wheel event to the outer chat. A subsequent wheel event scrolls the outer chat if it can still scroll in that direction.
  - [ ] Arrow keys scroll the focused element in the corresponding direction when it can scroll that way.
  - [ ] Page Up and Page Down scroll the focused element vertically when it can scroll.
  - [ ] Apply the behavior in the shared JavaFX chat renderer, including embedded use.

## Out-of-Scope / Future Enhancements
* Keyboard scrolling outside the chat renderer.
* Changes to scrollbar appearance or behavior beyond supporting wheel scrolling.
* Automatic scrolling to the newest message or kinetic/inertial scrolling.
* Scrolling behavior changes to the CLI or other non-JavaFX interfaces.

## Technical Context & Infrastructure
* **Development Toolchain**: Java, JavaFX, Maven, JUnit 5, TestFX
* **Target Systems/Platforms**: JavaFX desktop applications and host applications embedding the JavaFX chat renderer
* **Project Reference Docs**:
  - Coding Standard: `project/coding-standard.md`
  - Development Framework: `project/development-framework.md`
