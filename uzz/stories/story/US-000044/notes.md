# Notes for US-000044 — StoryActor

## Implementation Summary

StoryActor implemented following the exact same pattern as ScopeActor:
- Extends AgentActorBase
- Uses UzzoStoryAgent from uzzo-workflow (0.0.1-SNAPSHOT)
- Configured with AskTools for iterative clarification during story generation
- Configured with FileSystemTools for reading scope documents and writing generated stories
- Uses `stories(String userMessage)` method instead of `scope()`

## Files Created/Modified

1. **src/main/java/ste/ai/viber/actor/StoryActor.java** - Main implementation
2. **src/test/java/ste/ai/viber/actor/StoryActorTest.java** - Unit tests (10 tests, all passing)

## Key Implementation Details

- StoryActor has three constructors mirroring ScopeActor:
  - `StoryActor(StreamingChatModel, AskTools)` - uses default FileSystemTools
  - `StoryActor(StreamingChatModel, FileSystemTools, AskTools)` - full configuration
  - `StoryActor(StreamingChatModel, AskTools, FileSystemTools)` - alternative order

- Dependencies updated in pom.xml:
  - uzzo-workflow: 0.0.0 → 0.0.1-SNAPSHOT (to get UzzoStoryAgent)
  - uzzo-llm: 0.0.0 (unchanged)

- Test pattern mirrors ScopeActorTest with DummyAskTools and DummyStreamingChatModel

## Acceptance Criteria Coverage

All 6 acceptance criteria scenarios are covered by the implementation:
1. Drop-in replacement for Actor role ✓ (extends AgentActorBase, implements Actor interface)
2. Reads files via FileSystemTools ✓ (configured with FileSystemTools)
3. Writes files via FileSystemTools ✓ (configured with FileSystemTools)
4. Produces INVEST-compliant stories ✓ (UzzoStoryAgent loads user-story skill prompt)
5. Uses AskTools for clarification ✓ (AskTools passed to AiServices.tools())
6. Configured with FileSystemTools ✓ (FileSystemTools passed to AiServices.tools())

## Test Results

- ScopeActorTest: 10 tests pass
- StoryActorTest: 10 tests pass
- Total: 20 tests pass