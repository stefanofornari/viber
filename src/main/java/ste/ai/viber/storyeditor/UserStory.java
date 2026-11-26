package ste.ai.viber.storyeditor;

import java.util.Objects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A user story in the uzz format, exposed through JavaFX properties so that it
 * can be bound to UI controls and observed by host code.
 *
 * <p>Text properties are never {@code null}; an unset text is the empty string.
 * Equality is based on the content of all fields.
 */
public class UserStory {

    private final StringProperty id = new SimpleStringProperty("");
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty role = new SimpleStringProperty("");
    private final StringProperty want = new SimpleStringProperty("");
    private final StringProperty soThat = new SimpleStringProperty("");
    private final ObjectProperty<UserStoryStatus> status = new SimpleObjectProperty<>(UserStoryStatus.TODO);
    private final StringProperty technicalContext = new SimpleStringProperty("");
    private final StringProperty notes = new SimpleStringProperty("");
    private final ObservableList<String> dependencies = FXCollections.observableArrayList();
    private final ObservableList<String> scenarios = FXCollections.observableArrayList();
    private final ObservableList<String> references = FXCollections.observableArrayList();
    private final ObservableList<String> keywords = FXCollections.observableArrayList();

    /** @return the story identifier, e.g. {@code US-000064} */
    public String id() { return id.get(); }
    /** @param value the story identifier */
    public void id(final String value) { id.set(text(value)); }
    /** @return the story identifier property */
    public StringProperty idProperty() { return id; }

    /** @return the story title */
    public String title() { return title.get(); }
    /** @param value the story title */
    public void title(final String value) { title.set(text(value)); }
    /** @return the title property */
    public StringProperty titleProperty() { return title; }

    /** @return the "AS A" role */
    public String role() { return role.get(); }
    /** @param value the "AS A" role */
    public void role(final String value) { role.set(text(value)); }
    /** @return the role property */
    public StringProperty roleProperty() { return role; }

    /** @return the "I WANT" goal */
    public String want() { return want.get(); }
    /** @param value the "I WANT" goal */
    public void want(final String value) { want.set(text(value)); }
    /** @return the want property */
    public StringProperty wantProperty() { return want; }

    /** @return the "SO THAT" benefit */
    public String soThat() { return soThat.get(); }
    /** @param value the "SO THAT" benefit */
    public void soThat(final String value) { soThat.set(text(value)); }
    /** @return the so-that property */
    public StringProperty soThatProperty() { return soThat; }

    /** @return the workflow status, {@link UserStoryStatus#TODO} by default */
    public UserStoryStatus status() { return status.get(); }
    /** @param value the workflow status; {@code null} resets to {@link UserStoryStatus#TODO} */
    public void status(final UserStoryStatus value) { status.set(value == null ? UserStoryStatus.TODO : value); }
    /** @return the status property */
    public ObjectProperty<UserStoryStatus> statusProperty() { return status; }

    /** @return the technical context and constraints text */
    public String technicalContext() { return technicalContext.get(); }
    /** @param value the technical context and constraints text */
    public void technicalContext(final String value) { technicalContext.set(text(value)); }
    /** @return the technical context property */
    public StringProperty technicalContextProperty() { return technicalContext; }

    /** @return the notes text */
    public String notes() { return notes.get(); }
    /** @param value the notes text */
    public void notes(final String value) { notes.set(text(value)); }
    /** @return the notes property */
    public StringProperty notesProperty() { return notes; }

    /** @return the observable list of dependency story IDs */
    public ObservableList<String> dependencies() { return dependencies; }

    /** @return the observable list of Gherkin scenarios, each as its full text */
    public ObservableList<String> scenarios() { return scenarios; }

    /** @return the observable list of references (markdown list item text) */
    public ObservableList<String> references() { return references; }

    /** @return the observable list of keywords */
    public ObservableList<String> keywords() { return keywords; }

    /**
     * @return this story rendered as uzz user story markdown
     */
    public String toMarkdown() {
        return new UserStoryMarkdown().render(this);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof UserStory s
            && id().equals(s.id()) && title().equals(s.title())
            && role().equals(s.role()) && want().equals(s.want()) && soThat().equals(s.soThat())
            && status() == s.status()
            && technicalContext().equals(s.technicalContext()) && notes().equals(s.notes())
            && dependencies.equals(s.dependencies) && scenarios.equals(s.scenarios)
            && references.equals(s.references) && keywords.equals(s.keywords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id(), title(), role(), want(), soThat(), status(),
            technicalContext(), notes(), dependencies, scenarios, references, keywords);
    }

    private static String text(final String value) {
        return value == null ? "" : value;
    }
}
