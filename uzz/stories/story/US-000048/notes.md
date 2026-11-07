# Notes for US-000048 — GUI Integration: StoryActor Option

## Implementation Summary

Added StoryActor integration to the ViberFX GUI following the same pattern as ScopeActor (US-000035).

## Files Modified

1. **src/main/resources/ste/ai/viber/cli/MainAppWindow.fxml** - Added "Story" CheckMenuItem to Actor menu
2. **src/main/java/ste/ai/viber/cli/Constants.java** - Added LABEL_STORY_INDICATOR constant
3. **src/main/java/ste/ai/viber/cli/MainAppController.java** - Added:
   - `storyAgentItem` field with @FXML
   - `switchToStoryActor()` method
   - `isStoryPhaseActive()` method
   - Updated `updateActorMenuSelection()` to include StoryActor
   - Updated `handleSend()` to show story generation phase indicator

## Acceptance Criteria Coverage

All 4 acceptance criteria scenarios covered:
1. **Start Story Generation Phase via GUI menu** ✓ - "Story" menu item in Actor menu triggers `switchToStoryActor()`
2. **Switch actor to StoryActor via actor selector** ✓ - Same menu item serves as actor selector
3. **Visual indication distinguishes story generation phase** ✓ - Green "> Story Generation" label with distinct color
4. **GUI AskTools integration works with StoryActor** ✓ - Uses `ViberGUIAskTools` same as ScopeActor

## Key Implementation Details

- StoryActor requires API key (like ScopeActor and LangChain4jActor)
- Falls back to FXActor if no API key configured
- Uses green color for indicator to distinguish from orange scoping phase
- Properly hides devModeWindow when switching to StoryActor
- Preserves conversation history when switching actors

## Test Results

- MainAppControllerTest: 12 tests pass
- All actor tests (ScopeActorTest, StoryActorTest): 20 tests pass