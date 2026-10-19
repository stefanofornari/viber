# Project Name: Viber ScopeAgent Integration
**Version:** 0.1.0-SNAPSHOT

## Overall Description
Integration of the Uzzo Workflow's ScopeAgent (`UzzoScopeAgent` + `ScopeAgentService`) into Viber as a specialized actor for the scoping phase. This replaces the generic `LangChain4jActor` for scope definition tasks, enabling interactive gap analysis and structured scope document generation. This is the first of three planned Uzzo workflow phases (Scope → Story → Code) to be integrated into Viber.

## In-Scope Capabilities
* **ScopeActor Implementation**:
  - [ ] New `ScopeActor` class implementing `Actor` interface, wrapping `ScopeAgentService`
  - [ ] Integration with Viber's conversation/chat/message model
  - [ ] Streaming response handling compatible with Viber's `TokenStream` pattern
* **AskTools Implementation for Viber**:
  - [ ] `ViberCLIAskTools` implementing `AskTools` interface for CLI interaction
  - [ ] `ViberGUIAskTools` implementing `AskTools` for JavaFX GUI interaction
  - [ ] AskTools methods annotated with `@ToolPolicy(INTERACTIVE)` for human-in-the-middle flows
* **CLI Integration**:
  - [ ] New `--scope` / `--agent=scope` CLI option to select ScopeActor
  - [ ] Scope-specific system prompt configuration
  - [ ] Scope session persistence (conversation history maintained)
* **GUI Integration**:
  - [ ] Actor selection menu includes "ScopeAgent" option
  - [ ] Visual indication of scoping phase (distinct from general chat)
* **Dependency Management**:
  - [ ] Add `uzzo-workflow` as Maven dependency in Viber
  - [ ] Align LangChain4j versions (Viber: 1.20.2, Uzzo: 1.21.0)
* **Testing**:
  - [ ] Unit tests for `ScopeActor`, `ViberCLIAskTools` and `ViberGUIAskTools` implementations
  - [ ] Integration test for CLI scope session
  - [ ] Integration test for GUI scope session

## Out-of-Scope / Future Enhancements
* **Story Phase Integration**: User story generation from scope (planned Uzzo phase 2)
* **Code Phase Integration**: TDD implementation from stories (planned Uzzo phase 3)
* **Scope Document Persistence**: Auto-save scope.md to Uzz KB structure
* **Web/UI Platform AskTools**: Web-based AskTools implementation
* **Scope Versioning**: Iterative scope document versioning and diff

## Technical Context & Infrastructure
* **Development Toolchain**: Maven + JavaFX, Java 21, LangChain4j 1.21.0+
* **Target Systems/Platforms**: JavaFX desktop applications, Terminal application
* **Project Reference Docs**:
  - Coding Standard: `uzz/project/coding-standard.md`
  - Development Framework: `uzz/project/development-framework.md`

### Keywords
viber, uzzo-workflow, scope-agent, scoping-phase, langchain4j, actor, asktools, cli, guiframework