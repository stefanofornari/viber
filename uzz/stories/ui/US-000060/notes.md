# US-000060 Implementation Notes

## Component Integration

- Uses `fx-radial-menu:0.0.0-SNAPSHOT` (generic API is documented as `RadialMenu<T>`, but the shipped class is non-generic; the implementation uses the raw type).
- `RadialMenu` renders wedge labels and supports a bindable diameter, but it has no hub node and no public clear-selection API.
- The hub text is therefore a `Label` overlaid on the radial in a `StackPane`, styled through `.radial-hub` in `viber.css`. It is driven by the current actor plus a separate radial selection state, so Explore and a menu-selected LLM remain distinguishable.
- Menu-driven actor switches rebuild the `RadialMenu` instance to clear its wedge highlight, because the component exposes no `clearSelection` method. The selection callback and component selection styling are otherwise used unchanged.

## Layout

- The prompt `TextArea` and radial slot live in an `HBox` named `promptRow`.
- The radial slot has a fixed 125x125px square size, independent of the prompt height, and the radial diameter follows the slot width. It is top-aligned in the prompt row and has 10px top/left padding; the prompt row uses 10px spacing. Wedge label size is controlled with `-rm-label-font-size: 11px` on `.radial-menu`. Explore is visually preselected on startup through the radial menu programmatic `select()` API; this does not change the active actor, so a keyless startup stays in Manual mode.
- The standalone phase indicator label was removed; the hub carries the phase text. Coding initialization failures show `!` in the hub and keep the full message in a tooltip.
- `promptRow` receives the VBox grow priority that previously belonged to the prompt; the prompt still takes all remaining row width.

## Tests

- `MainAppControllerTest` covers wedge-to-actor switching, hub states, explore/LLM distinction, missing API key behavior, highlight clearing, diameter tracking on resize and AskTools insertion, and overlap checks against the indicator and conversation area.
