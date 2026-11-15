# fx-radial-menu

A reusable JavaFX radial menu component with AtlantaFX theming support.

## Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>com.github.stefanofornari</groupId>
    <artifactId>fx-radial-menu</artifactId>
    <version>0.0.0-SNAPSHOT</version>
</dependency>
```

The control only needs `javafx-graphics`. AtlantaFX is not pulled in transitively: add `atlantafx-base` yourself if you want to use the AtlantaFX integration theme.

### Basic Usage

```java
import ste.fx.control.radialmenu.MenuItem;
import ste.fx.control.radialmenu.RadialMenu;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.List;

public class MyApp extends Application {
    @Override
    public void start(Stage stage) {
        var items = List.of(
            new MenuItem<>("Action A", "actionA"),
            new MenuItem<>("Action B", "actionB"),
            new MenuItem<>("Action C", "actionC")
        );

        RadialMenu menu = new RadialMenu(items, selected -> {
            System.out.println("Selected: " + selected.label());
        });

        Pane root = new Pane(menu);
        menu.layoutXProperty().bind(root.widthProperty().subtract(menu.diameterProperty()).divide(2));
        menu.layoutYProperty().bind(root.heightProperty().subtract(menu.diameterProperty()).divide(2));

        stage.setScene(new Scene(root, 400, 400));
        stage.show();
    }
}
```

## API Reference

### `RadialMenu`

Extends `Pane`. Renders menu items as wedge-shaped segments around a central hub.

#### Constructors

| Constructor | Description |
|-------------|-------------|
| `RadialMenu(List<? extends MenuItem<?>> items)` | Creates menu with items, no selection callback |
| `RadialMenu(List<? extends MenuItem<?>> items, MenuItemSelectionCallback callback)` | Creates menu with items and selection callback |

#### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `setSelectionCallback(MenuItemSelectionCallback callback)` | `void` | Sets or updates the selection callback |
| `selectedItem()` | `MenuItem<?>` | Returns the currently selected item (null if none) |
| `items()` | `ObservableList<MenuItem<?>>` | Returns the list of menu items |
| `labels()` | `ObservableList<Text>` | Returns a read-only view of the label text nodes, in item order |
| `diameterProperty()` | `DoubleProperty` | Overall circular footprint (default `270`); bindable |
| `diameter()` / `diameter(double)` | `double` / `void` | Reads or sets the diameter; non-positive values render no wedges |
| `innerRadiusRatioProperty()` | `DoubleProperty` | Hub opening relative to the outer radius (default `60 / 135`); clamped to `[0, 1)` |
| `innerRadiusRatio()` / `innerRadiusRatio(double)` | `double` / `void` | Reads or sets the hub ratio |
| `hubVisibleProperty()` | `BooleanProperty` | Whether the hub is present (default `true`); bindable. When `false` the hub shrinks to radius 0 and the wedges reach the centre; `innerRadiusRatio` is kept and applies again when the hub is shown |
| `hubVisible()` / `hubVisible(boolean)` | `boolean` / `void` | Reads or sets the hub presence |
| `labelRadiusRatioProperty()` | `DoubleProperty` | Label distance ratio within the radial band (default `0.5`) |
| `labelRadiusRatio()` / `labelRadiusRatio(double)` | `double` / `void` | Reads or sets the label distance ratio; values are clamped to `[0, 1]` |
| `labelFontSizeProperty()` | `DoubleProperty` | Base label font size (default `13.5`, i.e. diameter x 0.05) |
| `labelFontSize()` / `labelFontSize(double)` | `double` / `void` | Reads or sets the base label font size |

### `MenuItem<T>`

Record representing a menu entry.

| Field | Type | Description |
|-------|------|-------------|
| `label` | `String` | Display text for the segment |
| `icon` | `Node` | Optional icon (can be null) |
| `actionPayload` | `T` | Generic payload returned on selection |

#### Factory Methods

```java
// Label + payload only
new MenuItem<>("Label", "payload");

// Label + icon + payload
new MenuItem<>("Label", iconNode, "payload");
```

### `MenuItemSelectionCallback`

Functional interface for selection notifications.

```java
@FunctionalInterface
public interface MenuItemSelectionCallback {
    void onSelection(MenuItem<?> selectedItem);
}
```

## CSS Customization Variables

The component uses **looked-up colors** for theming. Define these on the `.radial-menu` style class (or any ancestor) to customize appearance. Rules added to the `Scene` or a custom stylesheet override the built-in defaults.

| Variable | Built-in Default | Applies To |
|----------|------------------|------------|
| `-rm-segment-bg` | `#E0E0E0` (light gray) | Segment fill (default) |
| `-rm-segment-border` | `#CCCCCC` (medium gray) | Segment stroke (default) |
| `-rm-segment-bg-hover` | `#CCCCCC` (medium gray) | Segment fill (hover) |
| `-rm-segment-border-hover` | `#AAAAAA` (darker gray) | Segment stroke (hover) |
| `-rm-segment-bg-selected` | `#999999` (dark gray) | Segment fill (selected) |
| `-rm-segment-border-selected` | `#888888` (darker gray) | Segment stroke (selected) |
| `-rm-label-fill` | `#333333` | Label text fill (default) |
| `-rm-label-fill-hover` | `#333333` | Label text fill (hover) |
| `-rm-label-fill-selected` | `#FFFFFF` | Label text fill (selected) |

The AtlantaFX integration theme maps every variable to the corresponding `-color-*` token, so the menu follows the active theme automatically.

### Styleable Label Properties

Label sizing values are set through JavaFX styleable properties, not looked-up colours:

| CSS Property | Java Property | Default | Description |
|--------------|---------------|---------|-------------|
| `-rm-label-radius-ratio` | `labelRadiusRatio` | `0.5` | Label distance within the band (`0` = inner radius, `1` = outer radius) |
| `-rm-label-font-size` | `labelFontSize` | `diameter x 0.05` | Base label font size in pixels |

The default font size follows the current diameter. Explicitly setting `-rm-label-font-size` in CSS fixes the size and disables automatic shrinking; overflowing labels are truncated with an ellipsis instead.

### Sizing

The control's footprint is driven by the bindable `diameter` property, and its hub by `innerRadiusRatio` (outer radius = `diameter / 2`, inner radius = outer radius x ratio); setting `hubVisible` to `false` collapses the hub to radius 0 regardless of the ratio. The hub itself is just the opening, so any content you place there (the demo shows a centre circle and label) is yours to hide, for example by binding its `visible` property to `hubVisibleProperty()`. All wedges, hub, and labels scale when either property changes; layout bounds stay exactly `[0, 0, diameter, diameter]` and hover/selection never change the footprint. Content is clipped to that square, and segment strokes are a uniform 2 px in normal, hover, and selected states.

```java
radialMenu.diameterProperty().bind(promptInput.heightProperty());
```

### Labels

Each item's label is drawn upright at its wedge's mid-angle, centred on the label anchor. Labels are mouse-transparent, so clicking or hovering a label behaves exactly like clicking or hovering its wedge; the label's hover and selected colours follow the wedge state. When an item has an icon, the icon is shown above the label if both fit, otherwise the icon is shown alone. Labels shrink to 70% of the base font size before being truncated with an ellipsis. Icon and label are centred as a single unit, icon above label; when the wedge's usable box becomes too small, the text is dropped and only the icon (if any) remains.

### Example: Custom Theme

```css
.radial-menu {
    -rm-segment-bg: #2D2D2D;
    -rm-segment-border: #444444;
    -rm-segment-bg-hover: #3D3D3D;
    -rm-segment-border-hover: #555555;
    -rm-segment-bg-selected: #007ACC;
    -rm-segment-border-selected: #005A9E;
    -rm-label-fill: #F0F0F0;
    -rm-label-fill-hover: #FFFFFF;
    -rm-label-fill-selected: #FFFFFF;
}
```

### Default Theme (Optional)

For applications not using AtlantaFX, a default theme is provided with the same grayscale values as the built-in defaults. Load it as a Scene stylesheet:

```java
import javafx.application.Application;

public class MyApp extends Application {
    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(root, 450, 450);
        // Load default radial menu theme (optional, for non-AtlantaFX apps)
        scene.getStylesheets().add(
            getClass().getResource("/ste/fx/control/radialmenu/radial-menu-theme.css").toExternalForm()
        );
        // ... rest of your code
    }
}
```

### AtlantaFX Theme (Optional)

For applications using AtlantaFX, an integration theme is provided that maps `-rm-*` variables to AtlantaFX button colors. Apply the AtlantaFX theme first, then add the integration stylesheet to the Scene:

```java
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;

public class MyApp extends Application {
    @Override
    public void start(Stage stage) {
        // Apply AtlantaFX theme FIRST
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        Scene scene = new Scene(root, 450, 450);
        // Then load the radial menu AtlantaFX integration at scene level
        // This maps -rm-* variables to AtlantaFX button colors
        scene.getStylesheets().add(
            getClass().getResource("/ste/fx/control/radialmenu/radial-menu-atlantafx.css").toExternalForm()
        );
        // ... rest of your code
    }
}
```

**Note**: The integration theme must be loaded as a Scene (author) stylesheet so it overrides the built-in defaults. Add any application-specific stylesheet after it to override the theme variables.

## AtlantaFX Theming

The component integrates with [AtlantaFX](https://github.com/mkpaz/atlantafx) themes. Load the integration stylesheet (see above) to map the `-rm-*` variables to AtlantaFX button and foreground colors, so the menu matches your application's theme.

### Style Mapping

The radial menu's visual states map to AtlantaFX root-level palette tokens:

| Radial Menu State | AtlantaFX Variable | Description |
|-------------------|-------------------|-------------|
| Default | `-color-bg-subtle`, `-color-border-default` | Segment fill & stroke |
| Hover | `-color-base-1`, `-color-border-default` | Segment fill & stroke on hover |
| Selected | `-color-accent-emphasis` | Segment fill & stroke when selected |
| Label (default/hover) | `-color-fg-default` | Label text fill |
| Label (selected) | `-color-fg-emphasis` | Label text fill on the selected wedge |

### Using with AtlantaFX

```java
import atlantafx.base.theme.PrimerLight; // or PrimerDark, Dracula, CupertinoLight, etc.
import javafx.application.Application;

public class MyApp extends Application {
    @Override
    public void start(Stage stage) {
        // Apply AtlantaFX theme BEFORE creating scene
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        // ... rest of your code
    }
}
```

### Available AtlantaFX Themes (base module)

| Theme Class | Variant |
|-------------|---------|
| `PrimerLight` | Light (default GitHub-like) |
| `PrimerDark` | Dark |
| `Dracula` | Dark (popular community theme) |
| `CupertinoLight` | Light (macOS-like) |
| `CupertinoDark` | Dark (macOS-like) |
| `NordLight` | Light (Nord color palette) |
| `NordDark` | Dark (Nord color palette) |

See [AtlantaFX themes documentation](https://mkpaz.github.io/atlantafx/) for complete list.

## Demo Application

Run the included demo to see the component in action:

```bash
mvn javafx:run
```

The demo shows:
- 5-segment radial menu with centered hub
- Selection callback updating center label
- Hover and selected visual states
- AtlantaFX PrimerLight theme applied

## Build & Test

```bash
# Compile
mvn compile

# Run tests (headless, Monocle)
mvn test

# Package
mvn package
```

## Requirements

- Java 21+
- JavaFX 21+
- Maven 3.8+

## License

Apache License 2.0