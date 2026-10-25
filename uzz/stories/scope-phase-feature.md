# Project Name: Viber Scoping Phase Feature
**Version:** 0.1.0-SNAPSHOT

## 📝 Overall Description
Feature to enable a dedicated "Scoping Phase" in Viber where a specialized scoping agent replaces the generic LangChain4jActor to interact with users for feature scoping. During this phase, the agent engages in iterative clarification with the user to define feature boundaries, then outputs a structured scope document. This implements the first phase of a potential workflow (Scope → Story → Code).

## 🎯 In-Scope Capabilities
* **Scoping Phase Activation**:
  - [ ] Manual activation via menu item (GUI) to enter scoping phase
  - [ ] ScopeAgent serves as drop-in replacement for Actor role during phase
  - [ ] Conversation history maintained within scoping phase chat
* **Scoped Agent Interaction**:
  - [ ] Agent reads/writes files to gather context and document scope
  - [ ] Agent asks clarifying questions to user iteratively (max 3 at a time)
  - [ ] Agent refines scope based on user responses
  - [ ] Agent writes final scope output to appropriate location
* **Integration with Viber Infrastructure**:
  - [ ] Leverages existing `HumanInTheMiddleWrapper` for tool policy enforcement
  - [ ] Uses `@ToolPolicy` classifications for scoping-related tool methods
  - [ ] Compatible with Viber's conversation/chat/message model
  - [ ] Streaming response handling compatible with `TokenStream` pattern
* **Phase Transition**:
  - [ ] Clear indication when in scoping phase (visual in GUI, textual in CLI)
  - [ ] System suggests next steps after scoping phase completion
  - [ ] Return to normal operation (LangChain4jActor) after scoping exits

## 🚫 Out-of-Scope / Future Enhancements
* **Automatic phase triggering** based on conversation context
* **ScopeAgent instruction/configuration** (assumes existing agent knows how to conduct scoping)
* **Persistence of scope documents** to external knowledge bases
* **Integration with subsequent workflow phases** (Story generation, Code implementation)
* **Advanced scoping features** (versioning, diffing, templates)
* **Web-based AskTools implementations**
* **CLI command-line options** (--scope / --agent=scope) - deferring to menu-only for now

## 🛠️ Technical Context & Infrastructure
* **Development Toolchain**: Maven + JavaFX, Java 21, LangChain4j 1.20.2+
* **Target Systems/Platforms**: JavaFX desktop applications, Terminal application (ViberCLI)
* **Project Reference Docs**:
  - Coding Standard: `uzz/project/coding-standard.md`
  - Development Framework: `uzz/project/development-framework.md`

## 🔗 Related Stories
* [Scope Agent Integration](scope-agent-integration.md) - Technical implementation of integrating the Uzzo ScopeAgent into Viber