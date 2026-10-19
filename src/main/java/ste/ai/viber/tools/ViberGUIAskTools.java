package ste.ai.viber.tools;

import java.util.List;
import java.util.Optional;

import dev.langchain4j.agent.tool.Tool;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;
import ste.lazyjava.safe.Safe;
import ste.llm.tools.ToolPolicy;
import ste.llm.tools.ToolPolicy.Policy;
import ste.uzzo.agent.tools.AskTools;

/**
 * Tools to ask question to the user. Examples of invocations:
 *
 * <pre>
 * {"tool":"AskQuestion", "arguments": {"question":"What's your name?"}}
 * {"tool":"AskSingleChoice", "arguments": {"question": "Where do you live?", options: ["Rome", "Paris", "Rest of the World"]}}
 * {"tool":"AskMultipleChoice", "arguments": {"question": "Where have you worked?", options: ["Rome", "Paris", "London", "Other"]}}
 * {"tool":"AskConfirmation", "arguments": {"question":"Is this a java application?"}}
 * </pre>
 *
 */
public class ViberGUIAskTools implements AskTools, Safe {

    private final Window owner;

    public ViberGUIAskTools() {
        this(null);
    }

    public ViberGUIAskTools(final Window owner) {
        this.owner = owner;
    }

    @Override
    @Tool(name = "AskQuestion", value = "Ask the user an open-ended question and return their response")
    @ToolPolicy(Policy.INTERACTIVE)
    public String askQuestion(final String question) {
        requireNonNull(question, "question");

        return askQuestionDialog(question).orElse("");
    }

    @Override
    @Tool(name = "AskSingleChoice", value = "Ask the user to select one option from a list and return their choice")
    @ToolPolicy(Policy.INTERACTIVE)
    public String askSingleChoice(final String question, final List<String> options) {
        requireNonNull(question, "question");
        requireNonNull(options, "options");

        if (options.isEmpty()) {
            return "";
        }

        return askSingleChoiceDialog(question, options).orElse(options.get(0));
    }

    @Override
    @Tool(name = "AskMultipleChoice", value = "Ask the user to select multiple options from a listand return selected items")
    @ToolPolicy(Policy.INTERACTIVE)
    public List<String> askMultipleChoice(final String question, final List<String> options) {
        requireNonNull(question, "question");
        requireNonNull(options, "options");

        if (options.isEmpty()) {
            return List.of();
        }

        return askMultipleChoiceDialog(question, options).orElse(List.of());
    }

    @Override
    @Tool(name = "AskConfirmation", value = "Ask the user to confirm a decision (yes/no) and return their choice")
    @ToolPolicy(Policy.INTERACTIVE)
    public boolean askConfirmation(final String question) {
        requireNonNull(question, "question");

        return askConfirmationDialog(question).orElse(ButtonType.NO) == ButtonType.YES;
    }

    protected Optional<String> askQuestionDialog(final String question) {
        try {
            return runOnFxThreadWaitResult(() -> {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setHeaderText(question);
                dialog.setTitle("Question");
                applyOwner(dialog);
                return dialog.showAndWait();
            });
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    protected Optional<String> askSingleChoiceDialog(final String question, final List<String> options) {
        try {
            return runOnFxThreadWaitResult(() -> {
                ChoiceDialog<String> dialog = new ChoiceDialog<>(options.get(0), options);
                dialog.setHeaderText(question);
                dialog.setTitle("Select Option");
                applyOwner(dialog);
                return dialog.showAndWait();
            });
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    protected Optional<List<String>> askMultipleChoiceDialog(final String question, final List<String> options) {
        try {
            return runOnFxThreadWaitResult(() -> {
                Dialog<List<String>> dialog = new Dialog<>();
                dialog.setHeaderText(question);
                dialog.setTitle("Select Options");

                ListView<String> listView = new ListView<>(FXCollections.observableArrayList(options));
                listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                dialog.getDialogPane().setContent(listView);
                dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == ButtonType.OK) {
                        return listView.getSelectionModel().getSelectedItems();
                    }
                    return List.of();
                });

                applyOwner(dialog);
                return dialog.showAndWait();
            });
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    protected Optional<ButtonType> askConfirmationDialog(final String question) {
        try {
            return runOnFxThreadWaitResult(() -> {
                Alert dialog = new Alert(
                    AlertType.CONFIRMATION,
                    "",
                    ButtonType.YES,
                    ButtonType.NO
                );
                dialog.setHeaderText(question);
                dialog.setTitle("Confirmation");
                applyOwner(dialog);
                return dialog.showAndWait();
            });
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    private <T> T runOnFxThreadWaitResult(java.util.concurrent.Callable<T> task) throws Throwable {
        if (Platform.isFxApplicationThread()) {
            return task.call();
        } else {
            java.util.concurrent.atomic.AtomicReference<T> resultRef = new java.util.concurrent.atomic.AtomicReference<>();
            java.util.concurrent.atomic.AtomicReference<Throwable> exRef = new java.util.concurrent.atomic.AtomicReference<>();
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            javafx.application.Platform.runLater(() -> {
                try {
                    resultRef.set(task.call());
                } catch (Throwable e) {
                    exRef.set(e);
                } finally {
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                java.lang.Thread.currentThread().interrupt();
                throw e;
            }
            if (exRef.get() != null) {
                throw exRef.get();
            }
            return resultRef.get();
        }
    }

    private void applyOwner(final Dialog<?> dialog) {
        if (owner != null) {
            dialog.initOwner(owner);
        }
    }
}
