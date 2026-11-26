package ste.ai.viber.storyeditor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import atlantafx.base.theme.Styles;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * A JavaFX control to view and edit a {@link UserStory}.
 *
 * <p>The edited story is exposed by the {@link #storyProperty() story} property.
 * Setting it refreshes the UI, and editing the UI updates the story. With no story
 * the fields are empty and disabled. Styling comes from the host application's
 * AtlantaFX theme plus a small stylesheet for the scenario panes.
 */
public class UserStoryEditor extends VBox {

    // Room left in a scenario header for the expand arrow, borders and padding.
    private static final double HEADER_MARGIN = 60;
    private static final String SCENARIO_TEMPLATE =
        "Scenario: New scenario\n  Given \n  When \n  Then ";

    private final ObjectProperty<UserStory> story = new SimpleObjectProperty<>();
    private final ReadOnlyObjectWrapper<Exception> error = new ReadOnlyObjectWrapper<>();
    // Tracked here because UserStory equality is content-based, so change listeners
    // would skip replacing a story with an equal one.
    private UserStory bound;
    private boolean syncingDependencies;
    private final ListChangeListener<String> dependenciesListener =
        change -> syncDependencyText(change.getList());
    private boolean editingScenario;
    private final ListChangeListener<String> scenariosListener = change -> {
        if (!editingScenario) {
            loadRows();
            rebuildScenarios();
        }
    };
    // The rows shown, including the ones flagged as deleted. The story only holds the
    // others, so deleted rows are never saved but can still be restored.
    private final List<ScenarioRow> rows = new ArrayList<>();

    private static final class ScenarioRow {
        private String text;
        private boolean expanded;
        private boolean deleted;

        private ScenarioRow(final String text, final boolean expanded) {
            this.text = text;
            this.expanded = expanded;
        }
    }

    @FXML
    protected GridPane headerForm;
    @FXML
    protected TextField idField;
    @FXML
    protected TextField titleField;
    @FXML
    protected TextField roleField;
    @FXML
    protected TextArea wantArea;
    @FXML
    protected TextArea soThatArea;
    @FXML
    protected ComboBox<UserStoryStatus> statusBox;
    @FXML
    protected TextField dependenciesField;
    @FXML
    protected Tab criteriaTab;
    @FXML
    protected VBox criteriaForm;
    @FXML
    protected VBox notesForm;
    @FXML
    protected TextArea technicalContextArea;
    @FXML
    protected TextArea notesArea;
    @FXML
    protected ScrollPane scenarioScroll;
    @FXML
    protected VBox scenarioBox;
    @FXML
    protected Button addScenarioButton;

    /** Creates an editor with no story. */
    public UserStoryEditor() {
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("UserStoryEditor.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (final IOException x) {
            throw new UncheckedIOException(x);
        }

        getStylesheets().add(getClass().getResource("UserStoryEditor.css").toExternalForm());
        statusBox.getItems().setAll(UserStoryStatus.values());
        initializeDependencies();
        initializeScenarios();
        story.addListener(observable -> {
            final UserStory newStory = story.get();
            final UserStory oldStory = bound;
            bound = newStory;
            rebind(oldStory, newStory);
        });
        rebind(null, null);
    }

    /** @return the edited story, or {@code null} */
    public UserStory story() { return story.get(); }

    /** @param value the story to edit, or {@code null} to clear the editor */
    public void story(final UserStory value) { story.set(value); }

    /** @return the property holding the edited story */
    public ObjectProperty<UserStory> storyProperty() { return story; }

    /** @return the last load/save error, or {@code null} if the last operation succeeded */
    public Exception error() { return error.get(); }

    /** @return the read-only property notifying hosts of load and save errors */
    public ReadOnlyObjectProperty<Exception> errorProperty() { return error.getReadOnlyProperty(); }

    /**
     * Loads the story from a markdown file, replacing the current one. On failure
     * the current story is kept and the error is published on {@link #errorProperty()}.
     *
     * @param file the UTF-8 markdown file
     */
    public void load(final Path file) {
        try {
            load(Files.readString(file, StandardCharsets.UTF_8));
        } catch (final IOException x) {
            error.set(x);
        }
    }

    /**
     * Loads the story from markdown text, replacing the current one. On failure
     * the current story is kept and the error is published on {@link #errorProperty()}.
     *
     * @param markdown the story markdown
     */
    public void load(final String markdown) {
        try {
            final UserStory parsed = new UserStoryMarkdown().parse(markdown);
            error.set(null);
            story.set(parsed);
        } catch (final IllegalArgumentException x) {
            error.set(x);
        }
    }

    /**
     * Saves the current story as a UTF-8 markdown file. Errors, including having
     * no story, are published on {@link #errorProperty()}.
     *
     * @param file the target file
     */
    public void save(final Path file) {
        final UserStory current = story.get();
        if (current == null) {
            error.set(new IllegalStateException("no story to save"));
            return;
        }
        try {
            Files.writeString(file, current.toMarkdown(), StandardCharsets.UTF_8);
            error.set(null);
        } catch (final IOException x) {
            error.set(x);
        }
    }

    /** @return the current story as markdown, or an empty string if there is none */
    public String toMarkdown() {
        final UserStory current = story.get();
        return current == null ? "" : current.toMarkdown();
    }

    private void initializeScenarios() {
        addScenarioButton.setOnAction(e -> addScenario());
    }

    private void addScenario() {
        if (bound == null) {
            return;
        }
        rows.add(new ScenarioRow(SCENARIO_TEMPLATE, true));
        syncScenarios();
        rebuildScenarios();
    }

    private void setDeleted(final ScenarioRow row, final boolean deleted) {
        row.deleted = deleted;
        row.expanded = false;
        syncScenarios();
        rebuildScenarios();
    }

    // The first scenario wraps around to the last position.
    private void moveScenarioUp(final int from) {
        final int to = from == 0 ? rows.size() - 1 : from - 1;
        if (to == from) {
            return;
        }
        rows.add(to, rows.remove(from));
        syncScenarios();
        rebuildScenarios();
    }

    private void loadRows() {
        rows.clear();
        if (bound != null) {
            bound.scenarios().forEach(text -> rows.add(new ScenarioRow(text, false)));
        }
    }

    private void syncScenarios() {
        final List<String> kept = rows.stream().filter(row -> !row.deleted).map(row -> row.text).toList();
        if (kept.equals(bound.scenarios())) {
            return;
        }
        editingScenario = true;
        try {
            bound.scenarios().setAll(kept);
        } finally {
            editingScenario = false;
        }
    }

    private void rebuildScenarios() {
        scenarioBox.getChildren().clear();
        for (int i = 0; i < rows.size(); i++) {
            scenarioBox.getChildren().add(scenarioPane(i));
        }
        addScenarioButton.setDisable(bound == null);
    }

    private TitledPane scenarioPane(final int index) {
        final ScenarioRow row = rows.get(index);
        final Label title = new Label(scenarioTitle(row.text));
        title.getStyleClass().add("scenario-title");
        final Button up = iconButton(Feather.ARROW_UP, "scenario-up-button", "Move up");
        up.setOnAction(e -> moveScenarioUp(index));
        final TextArea text = new TextArea(row.text);
        text.getStyleClass().add("scenario-text");
        text.setPrefRowCount(6);
        text.setWrapText(true);
        final Button action;
        if (row.deleted) {
            // Still expandable, read-only, so the content can be checked before it is lost.
            title.getStyleClass().add("scenario-deleted");
            text.setEditable(false);
            action = iconButton(Feather.ROTATE_CCW, "scenario-undo-button", "Undo delete");
            action.setOnAction(e -> setDeleted(row, false));
        } else {
            action = iconButton(Feather.TRASH_2, "scenario-delete-button", "Delete");
            action.setOnAction(e -> setDeleted(row, true));
            text.textProperty().addListener((obs, old, value) -> {
                title.setText(scenarioTitle(value));
                row.text = value;
                syncScenarios();
            });
        }
        final TitledPane pane = headedPane(title, text, action, up);
        pane.setExpanded(row.expanded);
        pane.expandedProperty().addListener((obs, was, is) -> row.expanded = is);
        if (row.deleted) {
            pane.getStyleClass().add("scenario-deleted-pane");
        }
        return pane;
    }

    // The header holds the title and the action buttons; the title takes the free space.
    private TitledPane headedPane(final Label title, final TextArea content, final Button... buttons) {
        final HBox header = new HBox(4);
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(title, Priority.ALWAYS);
        title.setMaxWidth(Double.MAX_VALUE);
        header.getChildren().add(title);
        header.getChildren().addAll(buttons);
        final TitledPane pane = new TitledPane(null, content);
        pane.getStyleClass().add("scenario-pane");
        pane.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        pane.setGraphic(header);
        // Bound to the viewport, not to the pane: binding to the pane's own width
        // feeds back into its preferred width and makes it grow without limit.
        header.prefWidthProperty().bind(
            Bindings.createDoubleBinding(() -> scenarioScroll.getViewportBounds().getWidth() - HEADER_MARGIN,
                scenarioScroll.viewportBoundsProperty()));
        return pane;
    }

    private static Button iconButton(final Feather icon, final String styleClass, final String tip) {
        final Button button = new Button(null, FontIcon.of(icon));
        button.getStyleClass().addAll(Styles.FLAT, Styles.BUTTON_CIRCLE, Styles.BUTTON_ICON, styleClass);
        button.setTooltip(new Tooltip(tip));
        button.setAccessibleText(tip);
        button.setFocusTraversable(false);
        return button;
    }

    private static String scenarioTitle(final String text) {
        final String first = text.lines().findFirst().orElse("").strip();
        return first.isEmpty() ? "(untitled scenario)" : first;
    }

    private void initializeDependencies() {
        dependenciesField.textProperty().addListener((obs, oldText, text) -> {
            if (!syncingDependencies && bound != null) {
                final List<String> parsed = parseDependencies(text);
                if (!parsed.equals(bound.dependencies())) {
                    bound.dependencies().setAll(parsed);
                }
            }
        });
    }

    private void syncDependencyText(final List<? extends String> dependencies) {
        if (!parseDependencies(dependenciesField.getText()).equals(dependencies)) {
            setDependencyText(String.join(", ", dependencies));
        }
    }

    private void setDependencyText(final String text) {
        syncingDependencies = true;
        try {
            dependenciesField.setText(text);
        } finally {
            syncingDependencies = false;
        }
    }

    private List<String> parseDependencies(final String text) {
        return Arrays.stream(text.split(","))
            .map(String::strip)
            .filter(entry -> !entry.isEmpty())
            .distinct()
            .toList();
    }

    private void rebind(final UserStory oldStory, final UserStory newStory) {
        if (oldStory != null) {
            oldStory.dependencies().removeListener(dependenciesListener);
            oldStory.scenarios().removeListener(scenariosListener);
        }
        if (newStory != null) {
            newStory.dependencies().addListener(dependenciesListener);
            newStory.scenarios().addListener(scenariosListener);
        }
        loadRows();
        rebuildScenarios();
        setDependencyText(newStory == null ? "" : String.join(", ", newStory.dependencies()));
        if (oldStory != null) {
            idField.textProperty().unbindBidirectional(oldStory.idProperty());
            titleField.textProperty().unbindBidirectional(oldStory.titleProperty());
            roleField.textProperty().unbindBidirectional(oldStory.roleProperty());
            wantArea.textProperty().unbindBidirectional(oldStory.wantProperty());
            soThatArea.textProperty().unbindBidirectional(oldStory.soThatProperty());
            statusBox.valueProperty().unbindBidirectional(oldStory.statusProperty());
            technicalContextArea.textProperty().unbindBidirectional(oldStory.technicalContextProperty());
            notesArea.textProperty().unbindBidirectional(oldStory.notesProperty());
        }
        headerForm.setDisable(newStory == null);
        criteriaForm.setDisable(newStory == null);
        notesForm.setDisable(newStory == null);
        if (newStory == null) {
            idField.clear();
            titleField.clear();
            roleField.clear();
            wantArea.clear();
            soThatArea.clear();
            statusBox.setValue(null);
            technicalContextArea.clear();
            notesArea.clear();
            return;
        }
        idField.textProperty().bindBidirectional(newStory.idProperty());
        titleField.textProperty().bindBidirectional(newStory.titleProperty());
        roleField.textProperty().bindBidirectional(newStory.roleProperty());
        wantArea.textProperty().bindBidirectional(newStory.wantProperty());
        soThatArea.textProperty().bindBidirectional(newStory.soThatProperty());
        statusBox.valueProperty().bindBidirectional(newStory.statusProperty());
        technicalContextArea.textProperty().bindBidirectional(newStory.technicalContextProperty());
        notesArea.textProperty().bindBidirectional(newStory.notesProperty());
    }
}
