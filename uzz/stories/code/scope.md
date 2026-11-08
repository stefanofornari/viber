# Domain Name: Viber Coding Phase Feature

## Overall Description

Adds a coding phase to Viber's Scope/Story/Code workflow. The phase lets users select a `CodingActor` in Viber, which delegates to the existing `UzzoCodeAgent` from the uzzo-workflow agent library. This is an integration of an existing agent, not a new coding agent or an orchestration system.

The CodingActor is available through both ViberFX and ViberCLI and participates in the standard Viber conversation model. Its coding tools receive the existing `@ToolPolicy` classifications so future policy enforcement can act on them. This scope does not implement policy enforcement; annotations alone do not gate tool execution.

## In-Scope Capabilities

* **Coding Phase Activation**:
  - [ ] Allow users to select or start the CodingActor in the GUI and CLI
  - [ ] Use CodingActor as a drop-in replacement for the Actor role, delegating coding work to UzzoCodeAgent
  - [ ] Keep the coding session within Viber's existing conversation and message model
  - [ ] Keep the existing conversation history accessible when switching actors
* **Coding Tool Policy Metadata**:
  - [ ] Apply existing `@ToolPolicy` classifications to coding tool methods exposed to the agent, according to their behavior
  - [ ] Do not bypass or replace the existing tool metadata contract; runtime enforcement is not part of this phase
* **Phase Lifecycle and Indication**:
  - [ ] Clearly indicate the coding phase in the GUI and CLI
  - [ ] Support explicit user exit from the coding phase
  - [ ] Preserve access to coding-phase conversation history after returning to normal operation
* **Viber Integration**:
  - [ ] Follow the existing actor and streaming-response patterns
  - [ ] Use the existing UzzoCodeAgent implementation and its available coding tools without adding new agent behavior

## Out-of-Scope / Future Enhancements

* Implementing a new coding agent, model, coding prompt, or coding-tool set
* Runtime `@ToolPolicy` enforcement, human approval flows, or changes to interaction modes; annotations are preparatory only
* Automatically deciding to enter the coding phase based on conversation content
* Orchestrating scope generation, story generation, and code implementation as one autonomous workflow
* Backlog browsing, multi-story batch selection, or other story-management features
* Git commits, pushes, pull requests, releases, or deployment automation
* Automatically switch to other Actors when a developing wave is done
* Provide completion guidance after the coding phase ends -> this should be provided by the coding agent

## Technical Context & Infrastructure

* **Development Toolchain**: Java 21, Maven, JavaFX, LangChain4j 1.21.0, `uzzo-workflow` 0.0.1-SNAPSHOT, JUnit 5, TestFX
* **Target Systems/Platforms**: ViberFX JavaFX desktop application and ViberCLI terminal application
* **Project Reference Docs**:
  - Coding Standard: `uzz/project/coding-standard.md`
  - Development Framework: `uzz/project/development-framework.md`
  - Story Implementation Workflow: `uzz/skills/code/SKILL.md`
  - Scope Phase: `uzz/stories/scope/scope.md`
  - Story Phase: `uzz/stories/story/scope.md`
  - Tool Policy: `uzz/stories/actor/US-000024/US-000024.md`

**Implementation considerations**:
- The current tool-policy enforcement story (`US-000024`) is still marked TODO. Adding annotations does not itself prevent a coding tool from running; enforcement must be delivered separately before approval-gated execution can be relied upon.
- Existing scope/story phase stories note that agent completion detection is blocked. Automatic return therefore depends on UzzoCodeAgent exposing a reliable completion signal; explicit user exit remains available independently.

### Keywords
- coding-actor
- uzzo-code-agent
- coding-phase
- langchain4j
- tool-policy
- scope-story-code
