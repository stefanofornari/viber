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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javafx.scene.Node;

/**
 * A panel for displaying multiple questions in the chat window.
 * Extends TitledPane to look like a ToolMessagePane - shows tool name in title,
 * questions in content area with per-question Answer buttons.
 * Supports open-ended questions, single choice, multiple choice, and confirmation questions.
 * Each question has its own Answer button; when clicked, that question's future completes.
 */
public class QuestionsPanel extends TitledPane {

    public enum QuestionType {
        OPEN_ENDED("AskQuestion"),
        SINGLE_CHOICE("AskSingleChoice"),
        MULTIPLE_CHOICE("AskMultipleChoice"),
        CONFIRMATION("AskConfirmation");

        final String toolName;
        QuestionType(String toolName) { this.toolName = toolName; }
        public String getToolName() { return toolName; }
    }

    public static class Question {
        final QuestionType type;
        final String question;
        final List<String> options; // for choice questions
        final CompletableFuture<Object> future = new CompletableFuture<>();

        Question(QuestionType type, String question, List<String> options) {
            this.type = type;
            this.question = question;
            this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
        }
    }

    @FXML
    private VBox questionsContainer;

    @FXML
    private ScrollPane questionsScrollPane;

    private final Map<Question, NodeWithAnswer> questionNodes = new HashMap<>();
    private Consumer<Question> onQuestionAnswered;
    private Runnable onCancel;

    public QuestionsPanel() {
        loadFxml();
        getStyleClass().addAll("message", "message-questions");
        setMaxWidth(Double.MAX_VALUE);
        setExpanded(true);
        setAnimated(false);
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

    /**
     * Adds an open-ended question with its own Answer button.
     */
    public void addOpenEndedQuestion(String question) {
        Question q = new Question(QuestionType.OPEN_ENDED, question, null);
        addQuestionInternal(q, this::createTextFieldNode);
    }

    /**
     * Adds a single choice question with its own Answer button.
     */
    public void addSingleChoiceQuestion(String question, List<String> options) {
        Question q = new Question(QuestionType.SINGLE_CHOICE, question, options);
        addQuestionInternal(q, (qNode) -> createChoiceBoxNode(q, options));
    }

    /**
     * Adds a multiple choice question with its own Answer button.
     */
    public void addMultipleChoiceQuestion(String question, List<String> options) {
        Question q = new Question(QuestionType.MULTIPLE_CHOICE, question, options);
        addQuestionInternal(q, (qNode) -> createListViewNode(q, options));
    }

    /**
     * Adds a confirmation question with its own Answer button.
     */
    public void addConfirmationQuestion(String question) {
        Question q = new Question(QuestionType.CONFIRMATION, question, null);
        addQuestionInternal(q, this::createConfirmationNode);
    }

    private void addQuestionInternal(Question question, java.util.function.Function<Question, NodeWithAnswer> nodeCreator) {
        NodeWithAnswer node = nodeCreator.apply(question);
        questionNodes.put(question, node);

        // Wrap the question node with an Answer button
        HBox questionWrapper = new HBox(10);
        questionWrapper.getChildren().add(node.node);
        HBox.setHgrow(node.node, Priority.ALWAYS);

        Button answerBtn = new Button("Answer");
        answerBtn.setDefaultButton(true);
        answerBtn.getStyleClass().add("answer-button");
        answerBtn.setOnAction(e -> handleQuestionAnswer(question));

        questionWrapper.getChildren().add(answerBtn);
        questionsContainer.getChildren().add(questionWrapper);
    }

    private NodeWithAnswer createTextFieldNode(Question question) {
        VBox container = new VBox(5);
        container.getStyleClass().add("question-item");

        Label label = new Label(question.question);
        label.setWrapText(true);
        label.getStyleClass().add("question-label");

        TextArea textArea = new TextArea();
        textArea.setPromptText("Type your answer...");
        textArea.setPrefRowCount(3);
        textArea.setWrapText(true);
        textArea.getStyleClass().add("question-input");

        container.getChildren().addAll(label, textArea);
        return new NodeWithAnswer(container, () -> textArea.getText().trim());
    }

    private NodeWithAnswer createChoiceBoxNode(Question question, List<String> options) {
        VBox container = new VBox(5);
        container.getStyleClass().add("question-item");

        Label label = new Label(question.question);
        label.setWrapText(true);
        label.getStyleClass().add("question-label");

        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll(options);
        if (!options.isEmpty()) {
            choiceBox.getSelectionModel().select(0);
        }
        choiceBox.getStyleClass().add("question-input");

        container.getChildren().addAll(label, choiceBox);
        return new NodeWithAnswer(container, () -> choiceBox.getSelectionModel().getSelectedItem());
    }

    private NodeWithAnswer createListViewNode(Question question, List<String> options) {
        VBox container = new VBox(5);
        container.getStyleClass().add("question-item");

        Label label = new Label(question.question);
        label.setWrapText(true);
        label.getStyleClass().add("question-label");

        VBox checkBoxContainer = new VBox(3);
        Map<String, CheckBox> checkBoxes = new HashMap<>();
        for (String option : options) {
            CheckBox cb = new CheckBox(option);
            checkBoxes.put(option, cb);
            checkBoxContainer.getChildren().add(cb);
        }
        checkBoxContainer.getStyleClass().add("question-input");

        container.getChildren().addAll(label, checkBoxContainer);
        return new NodeWithAnswer(container, () -> {
            List<String> selected = new ArrayList<>();
            for (Map.Entry<String, CheckBox> entry : checkBoxes.entrySet()) {
                if (entry.getValue().isSelected()) {
                    selected.add(entry.getKey());
                }
            }
            return selected;
        });
    }

    private NodeWithAnswer createConfirmationNode(Question question) {
        VBox container = new VBox(5);
        container.getStyleClass().add("question-item");

        Label label = new Label(question.question);
        label.setWrapText(true);
        label.getStyleClass().add("question-label");

        ChoiceBox<Boolean> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll(true, false);
        choiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Boolean value) {
                return value ? "Yes" : "No";
            }
            @Override
            public Boolean fromString(String string) {
                return "Yes".equals(string);
            }
        });
        choiceBox.getSelectionModel().select(1); // Default to No
        choiceBox.getStyleClass().add("question-input");

        container.getChildren().addAll(label, choiceBox);
        return new NodeWithAnswer(container, () -> choiceBox.getSelectionModel().getSelectedItem());
    }

    private void handleQuestionAnswer(Question question) {
        NodeWithAnswer node = questionNodes.get(question);
        if (node == null) return;

        Object answer = node.answerSupplier.get();
        question.future.complete(answer);

        // Disable this question's UI
        Node questionNode = node.node;
        questionNode.setDisable(true);
        questionNode.setOpacity(0.6);

        // Find and disable the Answer button (it's in the parent HBox)
        if (questionNode.getParent() instanceof HBox wrapper) {
            wrapper.getChildren().filtered(n -> n instanceof Button).forEach(b -> b.setDisable(true));
        }

        // Notify listener
        if (onQuestionAnswered != null) {
            onQuestionAnswered.accept(question);
        }
    }

    public void setOnQuestionAnswered(Consumer<Question> onQuestionAnswered) {
        this.onQuestionAnswered = onQuestionAnswered;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    /**
     * Clears all questions and buttons, prepares for new questions.
     */
    public void clear() {
        questionsContainer.getChildren().clear();
        questionNodes.clear();
        setDisable(false);
        setExpanded(true);
        getStyleClass().remove("questions-panel-completed");
    }

    /**
     * Returns the number of questions currently in the panel.
     */
    public int questionCount() {
        return questionNodes.size();
    }

    private record NodeWithAnswer(javafx.scene.Node node, java.util.function.Supplier<Object> answerSupplier) {}
}