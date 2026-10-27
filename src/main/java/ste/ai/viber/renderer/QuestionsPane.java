package ste.ai.viber.renderer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import ste.ai.viber.tools.QuestionType;
import static ste.ai.viber.tools.QuestionType.CONFIRMATION;
import static ste.ai.viber.tools.QuestionType.MULTIPLE_CHOICE;
import static ste.ai.viber.tools.QuestionType.OPEN_ENDED;
import static ste.ai.viber.tools.QuestionType.SINGLE_CHOICE;
import ste.lazyjava.safe.Safe;

import ste.llm.chat.model.ToolInvocationMessage;
import ste.uzzo.agent.tools.NoAnswerException;
import ste.ai.viber.tools.QuestionJSONObject;

/**
 * A panel for displaying multiple questions in the chat window.
 * Extends TitledPane to look like a ToolMessagePane - shows tool name in title,
 * questions in content area with per-question Answer buttons.
 * Supports open-ended questions, single choice, multiple choice, and confirmation questions.
 * Each question has its own Answer button; when clicked, that question's future completes.
 *
 * TODO: review to bound the questions to the UI dinamically
 */
public class QuestionsPane extends TitledPane implements Safe {

    private static final Logger LOG = Logger.getLogger(QuestionsPane.class.getName());

    public static class Question {
        final QuestionType type;
        final String question;
        final List<String> options;
        final CompletableFuture<Object> future = new CompletableFuture<>();

        public Question(final ToolInvocationMessage question) {
            final QuestionJSONObject askInvocation = new QuestionJSONObject(
                question.invocation.name, question.invocation.arguments);
            this.type = QuestionType.from(question.invocation.name);
            this.question = askInvocation.question();
            this.options = askInvocation.choices();
        }
    }

    @FXML
    private VBox questionsContainer;

    @FXML
    private ScrollPane questionsScrollPane;

    private final Map<Question, QuestionController> questionControllers = new HashMap<>();

    public final ObservableList<Question> questions = FXCollections.observableArrayList();
    public final ListProperty<Question> questionsProperty = new SimpleListProperty<>(questions);

    public QuestionsPane() {
        loadFxml();
        getStyleClass().addAll("message", "message-questions");
        setMaxWidth(Double.MAX_VALUE);
        setExpanded(true);
        setAnimated(false);

        questions.addListener((ListChangeListener.Change<? extends Question> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(this::addQuestion);
                }
            }
        });
    }

    private void loadFxml() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("QuestionsPanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Sets the tool name to display in the title (e.g., "AskQuestion", "AskSingleChoice").
     */
    public void setToolName(String toolName) {
        setText("ACTOR / " + toolName);
    }

    private void addQuestion(final Question question) {
        QuestionController controller = new QuestionController(question);
        questionControllers.put(question, controller);
        questionsContainer.getChildren().add(controller.questionBox);
    }

    private final PseudoClass ANSWERED_PSEUDO_CLASS = PseudoClass.getPseudoClass("answered");

    /**
     * Clears all questions and buttons, prepares for new questions.
     */
    public void clear() {
        questionsContainer.getChildren().clear();
        questionControllers.clear();
        setDisable(false);
        setExpanded(true);
        getStyleClass().remove("questions-panel-completed");
    }

    /**
     * Returns the number of questions currently in the panel.
     */
    public int questionCount() {
        return questionControllers.size();
    }

    private void handleQuestionAnswer(Question question) {
        QuestionController controller = questionControllers.get(question);
        if (controller == null) return;

        Object answer = controller.getAnswer();
        LOG.log(Level.FINEST, "Question answered: type={0}, question={1}, answer={2}",
                new Object[]{question.type, question.question, answer});

        question.future.complete(answer);

        // Disable this question's UI - CSS will handle styling via pseudo-class
        controller.questionBox.pseudoClassStateChanged(ANSWERED_PSEUDO_CLASS, true);
        controller.answerButton.setDisable(true);

        // Remove the answered question from the pane
        questionControllers.remove(question);
        questionsContainer.getChildren().remove(controller.questionBox);

        // Close the pane when no questions remain
        if (questionsContainer.getChildren().isEmpty()) {
            setDisable(true);
            if (getParent() instanceof javafx.scene.layout.Pane parent) {
                parent.getChildren().remove(this);
            }
        }
    }

    private void handleQuestionCancel(final Question question) {
        question.future.completeExceptionally(new NoAnswerException());

        QuestionController controller = questionControllers.remove(question);
        if (controller != null) {
            questionsContainer.getChildren().remove(controller.questionBox);
        }

        // Close the pane when no questions remain
        if (questionsContainer.getChildren().isEmpty()) {
            setDisable(true);
            if (getParent() instanceof javafx.scene.layout.Pane parent) {
                parent.getChildren().remove(this);
            }
        }
    }

    /**
     * Controller for a single question row loaded from QuestionItem.fxml
     * All input widgets are defined in QuestionItem.fxml and shown/hidden based on question type.
     */
    private class QuestionController {
        final Question question;

        @FXML
        private VBox questionBox;

        @FXML
        private Label questionLabel;

        @FXML
        private Button answerButton;

        @FXML
        private VBox inputBox;

        @FXML
        private TextArea textAreaInput;

        @FXML
        private ChoiceBox<String> choiceBoxInput;

        @FXML
        private VBox multiChoiceBoxInput;

        private Map<String, CheckBox> checkBoxes = new HashMap<>();

        QuestionController(Question question) {
            this.question = question;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("QuestionItem.fxml"));
            loader.setController(this);
            try {
                this.questionBox = loader.load();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            this.questionLabel.setText(question.question);
            this.answerButton.setUserData(question);

            // Configure inputs based on question type
            configureInputs();
        }

        private void configureInputs() {
            // Hide all inputs first
            textAreaInput.setVisible(false);
            textAreaInput.setManaged(false);
            choiceBoxInput.setVisible(false);
            choiceBoxInput.setManaged(false);
            multiChoiceBoxInput.setVisible(false);
            multiChoiceBoxInput.setManaged(false);

            switch (question.type) {
                case OPEN_ENDED -> {
                    textAreaInput.setVisible(true);
                    textAreaInput.setManaged(true);
                }
                case SINGLE_CHOICE -> {
                    choiceBoxInput.setVisible(true);
                    choiceBoxInput.setManaged(true);
                    choiceBoxInput.getItems().addAll(question.options);
                    if (!question.options.isEmpty()) {
                        choiceBoxInput.getSelectionModel().select(0);
                    }
                }
                case MULTIPLE_CHOICE -> {
                    multiChoiceBoxInput.setVisible(true);
                    multiChoiceBoxInput.setManaged(true);
                    multiChoiceBoxInput.getChildren().clear();
                    checkBoxes.clear();
                    // TODO: use a proper component with property
                    for (String option : question.options) {
                        CheckBox cb = new CheckBox(option);
                        checkBoxes.put(option, cb);
                        multiChoiceBoxInput.getChildren().add(cb);
                    }
                }
                case CONFIRMATION -> {
                    choiceBoxInput.setVisible(true);
                    choiceBoxInput.setManaged(true);
                    choiceBoxInput.getItems().addAll("Yes", "No");
                    choiceBoxInput.getSelectionModel().select(0);
                }
            }
        }

        @FXML
        private void handleAnswer() {
            QuestionsPane.this.handleQuestionAnswer(question);
        }

        @FXML
        private void handleCancel() {
            QuestionsPane.this.handleQuestionCancel(question);
        }

        Object getAnswer() {
            return switch (question.type) {
                case OPEN_ENDED -> textAreaInput.getText().trim();
                case SINGLE_CHOICE, CONFIRMATION -> choiceBoxInput.getSelectionModel().getSelectedItem();
                case MULTIPLE_CHOICE -> {
                    List<String> selected = new ArrayList<>();
                    for (Map.Entry<String, CheckBox> entry : checkBoxes.entrySet()) {
                        if (entry.getValue().isSelected()) {
                            selected.add(entry.getKey());
                        }
                    }
                    yield selected;
                }
            };
        }
    }
}