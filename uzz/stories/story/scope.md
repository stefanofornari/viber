# Domain Name: Viber StoryActor Feature

## Overall Description
Feature to implement a dedicated "Story Generation Phase" in Viber where a specialized StoryActor replaces the generic Actor to interact with users for generating INVEST-compliant user stories from scope documents. During this phase, the agent uses UzzoStoryAgent (from uzzo-workflow) to read scope documents, engage in iterative clarification with the user via AskTools, and output individual user story files following the kb-man standard. This implements the second phase of the Scope → Story → Code workflow.

## In-Scope Capabilities
* **Story Generation Phase Activation**:
  - [ ] Manual activation via menu item (GUI) to enter story generation phase
  - [ ] Manual activation via CLI flag (`--story` or `--agent=story`)
  - [ ] StoryActor serves as drop-in replacement for Actor role during phase
  - [ ] Conversation history maintained within story generation phase chat
* **StoryActor**:
  - [ ] Extends AgentActorBase following ScopeActor pattern
  - [ ] Uses UzzoStoryAgent from uzzo-workflow with streaming chat model
  - [ ] Configured with FileSystemTools for reading scope docs and writing story files
  - [ ] Uses `stories(String userMessage)` method instead of `scope()`
  - [ ] System prompt loaded from shared user-story skill resource (uzz/skills/story/SKILL.md)
* **Scoped Agent Interaction**:
  - [ ] Agent reads scope.md files from project using kb-man skill guidance
  - [ ] Agent asks clarifying questions to user iteratively via AskTools (max 3 at a time)
  - [ ] Agent refines stories based on user responses
  - [ ] Agent writes individual story files to uzz/stories/<domain>/US-XXXXXX/
  - [ ] Generated stories follow INVEST principles with Gherkin acceptance criteria
* **Integration with Viber Infrastructure**:
  - [ ] Leverages existing `HumanInTheMiddleWrapper` for tool policy enforcement
  - [ ] Uses `@ToolPolicy` classifications for story generation tool methods
  - [ ] Compatible with Viber's conversation/chat/message model
  - [ ] Streaming response handling compatible with `TokenStream` pattern
  - [ ] Works with both CLI (ViberCLIAskTools) and GUI (ViberGUIAskTools) implementations
* **Phase Transition**:
  - [ ] Clear indication when in story generation phase (visual in GUI, textual in CLI)
  - [ ] System suggests next steps after story generation phase completion
  - [ ] Return to normal operation (LangChain4jActor) after story generation exits

## Out-of-Scope / Future Enhancements
* **Automatic phase triggering** based on conversation context or scope.md completion
* **StoryActor instruction/configuration** (assumes existing agent knows how to conduct story generation)
* **Persistence of story documents** to external knowledge bases beyond local files
* **Integration with subsequent workflow phases** (Code implementation from stories)
* **Advanced story generation features** (versioning, diffing, templates, story refinement)
* **Web-based AskTools implementations**
* **Bulk story generation** without human-in-the-middle clarification

## Technical Context & Infrastructure
* **Development Toolchain**: Maven + JavaFX, Java 21, LangChain4j 1.21.0+
* **Target Systems/Platforms**: JavaFX desktop applications, Terminal application (ViberCLI)
* **Project Reference Docs**:
  - Coding Standard: `uzz/project/coding-standard.md`
  - Development Framework: `uzz/project/development-framework.md`
  - User Story Standard: `uzz/skills/kb/references/user_story.md`
  - Scope Agent Integration: `uzz/stories/scope-agent-integration.md` (unchanged)

## Related Stories
* [ScopeActor](../scope/US-000031/US-000031.md) - Pattern to follow
* [GUI AskTools](../scope/US-000033/US-000033.md) - GUI interaction pattern
* [CLI AskTools](../scope/US-000032/US-000032.md) - CLI interaction pattern

### Keywords
- story-actor
- uzzo-workflow
- story-generation
- invest-compliant
- scope-story-code-workflow