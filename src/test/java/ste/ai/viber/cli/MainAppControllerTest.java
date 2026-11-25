package ste.ai.viber.cli;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.util.WaitForAsyncUtils;
import ste.ai.viber.cli.command.CLIOptions;
import ste.ai.viber.renderer.GUIRenderer;
import ste.ai.viber.renderer.QuestionsPane;
import ste.ai.viber.renderer.QuestionsPane.Question;
import ste.fx.control.radialmenu.RadialMenu;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.Conversation;
import ste.llm.chat.model.PromptMessage;
import ste.llm.chat.model.ToolInvocationMessage;
import ste.llm.tools.InvocationJSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.BDDAssertions.then;

@ExtendWith(TestNameLogger.class)
class MainAppControllerTest extends org.testfx.framework.junit5.ApplicationTest {

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        ViberApplication.HOST = new javafx.application.HostServices();
        stage.show();
    }

    @Test
    void controller_has_actor_menu() {
        MainAppController controller = loadController(null);
        WaitForAsyncUtils.waitForFxEvents();

        MenuBar menuBar = (MenuBar) controller.getRoot().lookup(".menu-bar");
        then(menuBar).isNotNull();

        Menu actorMenu = menuBar.getMenus().stream()
            .filter(m -> "Actor".equals(m.getText()))
            .findFirst()
            .orElse(null);
        then(actorMenu).isNotNull();
    }

    @Test
    void main_window_opens_with_an_empty_chat() {
        MainAppController controller = loadController(null);
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.conversation.getTabs()).hasSize(1);
        HBox header = (HBox) controller.conversation.getTabs().get(0).getGraphic();
        then(((Label) header.getChildren().get(0)).getText()).startsWith("Chat 1");
        Button archive = tabButton(controller.conversation, 0, "chat-tab-archive-button");
        Button forget = tabButton(controller.conversation, 0, "chat-tab-forget-button");
        then(archive).isNotNull();
        then(forget).isNotNull();
        then(archive.isFocusTraversable()).isFalse();
        then(forget.isFocusTraversable()).isFalse();
    }

    @Test
    void archive_button_shows_persistence_not_yet_implemented_message() {
        Conversation conversation = new Conversation().addChat(new Chat(new PromptMessage("Hello")));
        MainAppController controller = loadController(conversation, null);
        WaitForAsyncUtils.waitForFxEvents();

        Button archiveButton = tabButton(controller.conversation, 0, "chat-tab-archive-button");
        interact(archiveButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        AtomicReference<DialogPane> dialogPane = new AtomicReference<>();
        interact(() -> dialogPane.set(dialogPaneWithContent("persistence not yet implemented")));

        then(dialogPane.get()).isNotNull();
        then(dialogPane.get().getContentText()).isEqualTo("persistence not yet implemented");

        interact(() -> ((Button) dialogPane.get().lookupButton(ButtonType.OK)).fire());
        WaitForAsyncUtils.waitForFxEvents();
        then(controller.conversation.getTabs()).hasSize(1);
    }

    @Test
    void confirmed_forget_shows_persistence_placeholder_without_closing_the_tab() {
        Conversation conversation = new Conversation().addChat(new Chat(new PromptMessage("Hello")));
        MainAppController controller = loadController(conversation, null);
        WaitForAsyncUtils.waitForFxEvents();

        Button forgetButton = tabButton(controller.conversation, 0, "chat-tab-forget-button");
        interact(forgetButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        AtomicReference<DialogPane> confirmation = new AtomicReference<>();
        interact(() -> confirmation.set(dialogPaneWithContent("completely discarded")));
        then(confirmation.get()).isNotNull();
        then(controller.conversation.getTabs()).hasSize(1);

        interact(() -> dialogButton(confirmation.get(), "Forget").fire());
        WaitForAsyncUtils.waitForFxEvents();

        AtomicReference<DialogPane> unavailable = new AtomicReference<>();
        interact(() -> unavailable.set(dialogPaneWithContent("persistence not yet implemented")));
        then(unavailable.get()).isNotNull();
        interact(() -> ((Button) unavailable.get().lookupButton(ButtonType.OK)).fire());
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.conversation.getTabs()).hasSize(1);
    }

    @Test
    void controller_actor_menu_has_fxactor_and_langchain4j_options() {
        MainAppController controller = loadController(null);
        WaitForAsyncUtils.waitForFxEvents();

        MenuBar menuBar = (MenuBar) controller.getRoot().lookup(".menu-bar");
        Menu actorMenu = menuBar.getMenus().stream()
            .filter(m -> "Actor".equals(m.getText()))
            .findFirst()
            .orElseThrow();

        then(actorMenu.getItems().get(0).getText()).isEqualTo("Manual");
        then(actorMenu.getItems().get(1).getText()).isEqualTo("LLM");
        then(actorMenu.getItems().get(2).getText()).isEqualTo("Scope");
        then(actorMenu.getItems().get(3).getText()).isEqualTo("Story");
        then(actorMenu.getItems().get(4).getText()).isEqualTo("Coding");
    }

    @Test
    void controller_fxactor_selected_by_default() {
        MainAppController controller = loadController(null);
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.getCurrentActorType()).isEqualTo("FXActor");
    }

    @Test
    void controller_switching_to_fxactor_makes_it_active() {
        MainAppController controller = loadController(null);
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.getCurrentActorType()).isEqualTo("FXActor");

        CheckMenuItem langChainItem = (CheckMenuItem) getActorMenuItem(controller, 1);
        interact(() -> {
            langChainItem.setSelected(true);
            langChainItem.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.getCurrentActorType()).isEqualTo("FXActor");
    }

    @Test
    void controller_langchain4jactor_selection_shows_information_dialog() {
        MainAppController controller = loadController(null);
        WaitForAsyncUtils.waitForFxEvents();

        CheckMenuItem langChainItem = (CheckMenuItem) getActorMenuItem(controller, 1);
        interact(() -> {
            langChainItem.setSelected(true);
            langChainItem.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.getCurrentActorType()).isEqualTo("FXActor");
    }

    @Test
    void controller_langchain4jactor_switches_when_key_is_provided() {
        CLIOptions options = new CLIOptions();
        options.key("test-key");

        MainAppController controller = loadController(options);
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.getCurrentActorType()).isEqualTo("LangChain4jActor");
    }

    @Test
    void controller_storyactor_switches_when_key_is_provided() {
        CLIOptions options = new CLIOptions();
        options.key("test-key");

        MainAppController controller = loadController(options);
        WaitForAsyncUtils.waitForFxEvents();

        CheckMenuItem storyItem = (CheckMenuItem) getActorMenuItem(controller, 3);
        interact(() -> {
            storyItem.setSelected(true);
            storyItem.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.getCurrentActorType()).isEqualTo("StoryActor");
        then(controller.isStoryPhaseActive()).isTrue();
        then(controller.isScopePhaseActive()).isFalse();
    }

    @Test
    void controller_coding_actor_switches_and_shows_phase_indicator() {
        CLIOptions options = new CLIOptions();
        options.key("test-key");
        MainAppController controller = loadController(options);
        WaitForAsyncUtils.waitForFxEvents();

        CheckMenuItem codingItem = (CheckMenuItem) getActorMenuItem(controller, 4);
        interact(() -> {
            codingItem.setSelected(true);
            codingItem.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.getCurrentActorType()).isEqualTo("CodingActor");
        then(controller.isCodingPhaseActive()).isTrue();
        then(codingItem.isSelected()).isTrue();
    }

    @Test
    void controller_keeps_previous_actor_selected_and_reports_coding_initialization_failure() {
        MainAppController controller = loadController(null);
        CheckMenuItem codingItem = (CheckMenuItem) getActorMenuItem(controller, 4);

        interact(() -> {
            codingItem.setSelected(true);
            codingItem.fire();
        });

        then(controller.getCurrentActorType()).isEqualTo("FXActor");
        then(((CheckMenuItem) getActorMenuItem(controller, 0)).isSelected()).isTrue();
        then(codingItem.isSelected()).isFalse();
        then(hub(controller).getText()).isEqualTo("!");
        then(hub(controller).getTooltip().getText()).contains("API key");
    }

    @Test
    void controller_ask_tools_pane_appears_for_storyactor() {
        CLIOptions options = new CLIOptions();
        options.key("test-key");
        MainAppController controller = loadController(new Conversation(), options);
        WaitForAsyncUtils.waitForFxEvents();

        CheckMenuItem storyItem = (CheckMenuItem) getActorMenuItem(controller, 3);
        interact(() -> {
            storyItem.setSelected(true);
            storyItem.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Verify StoryActor is active
        then(controller.getCurrentActorType()).isEqualTo("StoryActor");

        // When human() is called with a QuestionsPane (simulating AskTool invocation)
        VBox inputArea = (VBox) ((SplitPane) ((VBox) controller.getRoot()).getChildren().get(1)).getItems().get(1);
        HBox promptRow = (HBox) inputArea.getChildren().get(0);
        TextArea prompt = (TextArea) promptRow.getChildren().get(1);

        QuestionsPane questionsPane = new QuestionsPane();
        questionsPane.setToolName("AskQuestion");
        interact(() -> controller.human(questionsPane));
        WaitForAsyncUtils.waitForFxEvents();

        // Then the QuestionsPane should be inserted above the prompt
        then(inputArea.getChildren()).hasSize(2);
        then(inputArea.getChildren().get(0)).isSameAs(questionsPane);
        then(inputArea.getChildren().get(1)).isSameAs(promptRow);
        then(promptRow.getChildren().get(1)).isSameAs(prompt);
    }

    @Test
    void fxml_injects_conversation_renderer() {
        MainAppController controller = loadController(null);
        WaitForAsyncUtils.waitForFxEvents();

        SplitPane splitPane = (SplitPane) ((VBox) controller.getRoot()).getChildren().get(1);
        then(splitPane.getItems().get(0)).isInstanceOf(GUIRenderer.class);
    }

    @Test
    void fxml_renderer_displays_conversation() {
        Conversation conversation = new Conversation().addChat(new Chat(new PromptMessage("Hello")));
        MainAppController controller = loadController(conversation, null);
        WaitForAsyncUtils.waitForFxEvents();

        SplitPane splitPane = (SplitPane) ((VBox) controller.getRoot()).getChildren().get(1);
        then(splitPane.getItems().get(0)).isInstanceOf(GUIRenderer.class);
    }

    @Test
    void main_window_splits_output_and_bottom_input_sections() {
        MainAppController controller = loadController(null);
        VBox root = (VBox) controller.getRoot();
        SplitPane splitPane = (SplitPane) root.getChildren().get(1);

        then(root.getChildren()).hasSize(2);
        then(splitPane.getItems()).hasSize(2);
        then(splitPane.getItems().get(0)).isInstanceOf(GUIRenderer.class);
        then(splitPane.getItems().get(1)).isInstanceOf(VBox.class);
        then(splitPane.getDividerPositions()[0]).isEqualTo(0.75);
    }

    @Test
    void main_window_starts_centered_with_top_and_bottom_margins() {
        double[][] bounds = new double[1][];
        interact(() -> {
            Stage testStage = new Stage();
            ViberApplication.configureMainWindow(testStage, new javafx.geometry.Rectangle2D(100, 50, 1200, 900));
            bounds[0] = new double[]{testStage.getWidth(), testStage.getHeight(), testStage.getX(), testStage.getY()};
            testStage.close();
        });

        then(bounds[0]).containsExactly(800, 850, 300, 75);
    }

    @Test
    void radial_control_is_left_of_prompt_with_four_modes() {
        MainAppController controller = loadController(null);
        layout(controller, 800, 700);

        HBox promptRow = (HBox) controller.getRoot().lookup("#promptRow");
        StackPane radialSlot = (StackPane) controller.getRoot().lookup("#radialSlot");
        RadialMenu radial = radialMenu(controller);

        then(promptRow.getChildren().get(0)).isSameAs(radialSlot);
        then(promptRow.getChildren().get(1)).isInstanceOf(TextArea.class);
        then(radialSlot.getChildren().get(0)).isSameAs(radial);
        then(radial.labels()).extracting(javafx.scene.text.Text::getText)
            .containsExactly("Scope", "Story", "Code", "Explore");
    }

    @Test
    void radial_preselects_explore_on_startup() {
        MainAppController controller = loadController(null);
        layout(controller, 800, 700);

        then(controller.getCurrentActorType()).isEqualTo("FXActor");
        then(segmentIsSelected(controller, 3)).isTrue();
    }

    @Test
    void radial_wedge_selects_scope_actor_and_updates_hub() {
        CLIOptions options = new CLIOptions();
        options.key("test-key");
        MainAppController controller = loadController(options);
        layout(controller, 800, 700);

        fireWedge(controller, 0);
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.getCurrentActorType()).isEqualTo("ScopeActor");
        then(segmentIsSelected(controller, 0)).isTrue();
    }

    @Test
    void radial_wedge_selects_story_actor() {
        CLIOptions options = new CLIOptions();
        options.key("test-key");
        MainAppController controller = loadController(options);
        layout(controller, 800, 700);

        fireWedge(controller, 1);
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.getCurrentActorType()).isEqualTo("StoryActor");
    }

    @Test
    void radial_wedge_selects_coding_actor() {
        CLIOptions options = new CLIOptions();
        options.key("test-key");
        MainAppController controller = loadController(options);
        layout(controller, 800, 700);

        fireWedge(controller, 2);
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.getCurrentActorType()).isEqualTo("CodingActor");
    }

    @Test
    void radial_explore_wedge_selects_langchain4j_actor() {
        CLIOptions options = new CLIOptions();
        options.key("test-key");
        MainAppController controller = loadController(options);
        layout(controller, 800, 700);

        fireWedge(controller, 3);
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.getCurrentActorType()).isEqualTo("LangChain4jActor");
    }

    @Test
    void radial_code_wedge_without_key_preserves_actor_and_reports_failure() {
        MainAppController controller = loadController(null);
        layout(controller, 800, 700);

        fireWedge(controller, 2);
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.getCurrentActorType()).isEqualTo("FXActor");
        then(hub(controller).getText()).isEqualTo("!");
        then(hub(controller).getTooltip().getText()).contains("API key");
        then(segmentIsSelected(controller, 2)).isFalse();
    }

    @Test
    void menu_actor_switch_clears_radial_highlight() {
        CLIOptions options = new CLIOptions();
        options.key("test-key");
        MainAppController controller = loadController(options);
        layout(controller, 800, 700);

        fireWedge(controller, 0);
        WaitForAsyncUtils.waitForFxEvents();
        then(segmentIsSelected(controller, 0)).isTrue();

        CheckMenuItem storyItem = (CheckMenuItem) getActorMenuItem(controller, 3);
        interact(() -> {
            storyItem.setSelected(true);
            storyItem.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.getCurrentActorType()).isEqualTo("StoryActor");
        then(radialHasNoHighlight(controller)).isTrue();
    }

    @Test
    void menu_manual_and_llm_show_neutral_hub() {
        CLIOptions options = new CLIOptions();
        options.key("test-key");
        MainAppController controller = loadController(options);
        layout(controller, 800, 700);

        fireWedge(controller, 3);
        WaitForAsyncUtils.waitForFxEvents();
        then(hub(controller).getText()).isEqualTo(Constants.LABEL_HUB_NEUTRAL);

        CheckMenuItem llmItem = (CheckMenuItem) getActorMenuItem(controller, 1);
        interact(() -> {
            llmItem.setSelected(true);
            llmItem.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.getCurrentActorType()).isEqualTo("LangChain4jActor");
        then(hub(controller).getText()).isEqualTo(Constants.LABEL_HUB_NEUTRAL);

        CheckMenuItem manualItem = (CheckMenuItem) getActorMenuItem(controller, 0);
        interact(() -> {
            manualItem.setSelected(true);
            manualItem.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.getCurrentActorType()).isEqualTo("FXActor");
        then(hub(controller).getText()).isEqualTo(Constants.LABEL_HUB_NEUTRAL);
    }

    @Test
    void radial_has_fixed_size_and_is_top_aligned() {
        MainAppController controller = loadController(null);
        layout(controller, 800, 700);

        TextArea prompt = prompt(controller);
        RadialMenu radial = radialMenu(controller);

        StackPane radialSlot = (StackPane) controller.getRoot().lookup("#radialSlot");

        HBox promptRow = (HBox) controller.getRoot().lookup("#promptRow");

        then(radialSlot.getHeight()).isEqualTo(125.0);
        then(radialSlot.getWidth()).isEqualTo(125.0);
        then(radial.diameter()).isCloseTo(125.0, within(1.0));
        then(radial.labelFontSize()).isEqualTo(11.0);
        then(radial.getWidth()).isCloseTo(radial.getHeight(), within(1.0));
        then(radial.getWidth()).isCloseTo(radial.diameter(), within(1.0));
        then(promptRow.getSpacing()).isEqualTo(10.0);
        then(promptRow.getPadding().getLeft()).isEqualTo(0.0);
        then(promptRow.getAlignment()).isEqualTo(Pos.TOP_LEFT);
        then(radialSlot.localToScene(radialSlot.getBoundsInLocal()).getMinY())
            .isCloseTo(promptRow.localToScene(promptRow.getBoundsInLocal()).getMinY(), within(1.0));

        layout(controller, 800, 500);

        then(radial.diameter()).isCloseTo(radialSlot.getWidth(), within(1.0));
        then(radial.getWidth()).isCloseTo(radial.getHeight(), within(1.0));
    }

    @Test
    void radial_tracks_prompt_height_when_questions_pane_is_inserted_and_stays_clear() {
        MainAppController controller = loadController(null);
        layout(controller, 800, 800);

        QuestionsPane pane = new QuestionsPane();
        pane.questions.add(new Question(new ToolInvocationMessage(
            new InvocationJSONObject("AskQuestion", new JSONObject(Map.of(
                "question", "What should the application do?"
            )))
        )));

        interact(() -> {
            controller.human(pane);
            layoutNow(controller, 800, 800);
        });

        TextArea prompt = prompt(controller);
        RadialMenu radial = radialMenu(controller);

        then(prompt.getHeight()).isEqualTo(75);
        then(radial.diameter()).isCloseTo(125.0, within(1.0));
        then(radial.getWidth()).isCloseTo(radial.getHeight(), within(1.0));

        Bounds radialBounds = radial.localToScene(radial.getBoundsInLocal());
        GUIRenderer conversation = (GUIRenderer) controller.getRoot().lookup("#conversation");
        Bounds conversationBounds = conversation.localToScene(conversation.getBoundsInLocal());

        then(conversationBounds.getMaxY()).isLessThanOrEqualTo(radialBounds.getMinY());
    }

    private RadialMenu radialMenu(MainAppController controller) {
        return (RadialMenu) controller.getRoot().lookup("#radialMenu");
    }

    private Label hub(MainAppController controller) {
        return controller.radialHub();
    }

    private TextArea prompt(MainAppController controller) {
        HBox promptRow = (HBox) controller.getRoot().lookup("#promptRow");
        return (TextArea) promptRow.getChildren().get(1);
    }

    private void fireWedge(MainAppController controller, int index) {
        RadialMenu radial = radialMenu(controller);
        Node wedge = radial.getChildren().get(index);
        interact(() -> wedge.fireEvent(new MouseEvent(
            MouseEvent.MOUSE_CLICKED, 1, 1, 1, 1, MouseButton.PRIMARY, 1,
            false, false, false, false, true, false, false, true, false, false, null
        )));
    }

    private boolean segmentIsSelected(MainAppController controller, int index) {
        Node wedge = radialMenu(controller).getChildren().get(index);
        return wedge.getPseudoClassStates().contains(javafx.css.PseudoClass.getPseudoClass("selected"));
    }

    private boolean radialHasNoHighlight(MainAppController controller) {
        RadialMenu radial = radialMenu(controller);
        return radial.getChildren().stream()
            .noneMatch(node -> node.getPseudoClassStates()
                .contains(javafx.css.PseudoClass.getPseudoClass("selected")));
    }

    private void layout(MainAppController controller, double width, double height) {
        interact(() -> layoutNow(controller, width, height));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void layoutNow(MainAppController controller, double width, double height) {
        Parent root = (Parent) controller.getRoot();
        if (root.getScene() == null) {
            Scene scene = ste.ai.viber.test.FXTestSupport.styled(new Scene(root, width, height));
            stage.setScene(scene);
            stage.show();
        } else {
            stage.setWidth(width);
            stage.setHeight(height);
        }
        ((Region) root).resize(width, height);
        root.applyCss();
        root.layout();
    }

    private javafx.scene.control.MenuItem getActorMenuItem(MainAppController controller, int index) {
        MenuBar menuBar = (MenuBar) controller.getRoot().lookup(".menu-bar");
        Menu actorMenu = menuBar.getMenus().stream()
            .filter(m -> "Actor".equals(m.getText()))
            .findFirst()
            .orElseThrow();
        return actorMenu.getItems().get(index);
    }

    private Button tabButton(GUIRenderer renderer, int tabIndex, String styleClass) {
        HBox actions = (HBox) renderer.getTabs().get(tabIndex).getGraphic();
        return actions.getChildren().stream()
            .filter(Button.class::isInstance)
            .map(Button.class::cast)
            .filter(button -> button.getStyleClass().contains(styleClass))
            .findFirst()
            .orElseThrow();
    }

    private DialogPane dialogPaneWithContent(String content) {
        return Window.getWindows().stream()
            .filter(Window::isShowing)
            .filter(window -> window.getScene() != null)
            .map(window -> window.getScene().getRoot().lookup(".dialog-pane"))
            .filter(DialogPane.class::isInstance)
            .map(DialogPane.class::cast)
            .filter(dialog -> dialog.getContentText().contains(content))
            .findFirst()
            .orElseThrow();
    }

    private Button dialogButton(DialogPane dialogPane, String text) {
        return dialogPane.getButtonTypes().stream()
            .filter(buttonType -> buttonType.getText().equals(text))
            .map(dialogPane::lookupButton)
            .map(Button.class::cast)
            .findFirst()
            .orElseThrow();
    }

    private MainAppController loadController() {
        return loadController(null);
    }

    private MainAppController loadController(CLIOptions options) {
        return loadController(new Conversation(), options);
    }

    private MainAppController loadController(Conversation conversation, CLIOptions options) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainAppWindow.fxml"));
            Parent root = loader.load();
            MainAppController controller = loader.getController();
            AtomicReference<Stage> ownerRef = new AtomicReference<>();
            interact(() -> ownerRef.set(new Stage()));
            Stage owner = ownerRef.get();
            controller.initialize(conversation, options, owner);
            return controller;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void ask_tools_pane_appears_above_prompt_text_area() {
        // Given a controller with ScopeActor (which uses AskTools)
        CLIOptions options = new CLIOptions();
        options.key("test-key");
        MainAppController controller = loadController(new Conversation(), options);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify initial structure: [MenuBar, SplitPane]
        VBox root = (VBox) controller.getRoot();
        then(root.getChildren()).hasSize(2);
        SplitPane splitPane = (SplitPane) root.getChildren().get(1);
        then(splitPane.getItems().get(0)).isInstanceOf(GUIRenderer.class);
        VBox inputArea = (VBox) splitPane.getItems().get(1);
        HBox promptRow = (HBox) inputArea.getChildren().get(0);
        TextArea prompt = (TextArea) promptRow.getChildren().get(1);
        then(prompt.getId()).isEqualTo("prompt");

        // When human() is called with a QuestionsPane (simulating AskTool invocation)
        QuestionsPane questionsPane = new QuestionsPane();
        questionsPane.setToolName("AskQuestion");
        interact(() -> controller.human(questionsPane));
        WaitForAsyncUtils.waitForFxEvents();

        // Then the QuestionsPane should be inserted above the prompt within the input section
        then(inputArea.getChildren()).hasSize(2);
        then(inputArea.getChildren().get(0)).isSameAs(questionsPane);
        then(inputArea.getChildren().get(1)).isSameAs(promptRow);
    }

    @Test
    void multiple_choice_question_uses_content_height_when_window_has_space() {
        MainAppController controller = loadController(new Conversation(), null);
        VBox root = (VBox) controller.getRoot();
        QuestionsPane pane = new QuestionsPane();
        pane.questions.add(new Question(new ToolInvocationMessage(
            new InvocationJSONObject("AskMultipleChoice", new JSONObject(Map.of(
                "question", "Choose all applicable options",
                "options", List.of("Web", "Mobile", "Desktop")
            )))
        )));

        interact(() -> {
            controller.human(pane);
            stage.setScene(ste.ai.viber.test.FXTestSupport.styled(new Scene(root, 700, 800)));
            root.applyCss();
            root.layout();
        });

        then(pane.getHeight()).isLessThan(500);
    }

    @Test
    void ask_tools_pane_expands_when_input_section_is_resized() {
        MainAppController controller = loadController(new Conversation(), null);
        VBox root = (VBox) controller.getRoot();
        SplitPane splitPane = (SplitPane) root.getChildren().get(1);
        VBox inputArea = (VBox) splitPane.getItems().get(1);
        HBox promptRow = (HBox) inputArea.getChildren().get(0);
        TextArea prompt = (TextArea) promptRow.getChildren().get(1);
        QuestionsPane pane = new QuestionsPane();
        pane.questions.add(new Question(new ToolInvocationMessage(
            new InvocationJSONObject("AskQuestion", new JSONObject(Map.of(
                "question", "What should the application do?"
            )))
        )));
        double[] paneHeights = new double[2];

        interact(() -> {
            controller.human(pane);
            stage.setScene(ste.ai.viber.test.FXTestSupport.styled(new Scene(root, 700, 900)));
            stage.show();
            root.applyCss();
            root.resize(700, 900);
            root.layout();
            splitPane.setDividerPositions(0.75);
            root.layout();
            paneHeights[0] = pane.getHeight();

            splitPane.setDividerPositions(0.55);
            root.resize(700, 900);
            root.layout();
            paneHeights[1] = pane.getHeight();
        });

        then(prompt.getHeight()).isEqualTo(75);
        then(paneHeights[1]).isGreaterThan(paneHeights[0]);

        interact(() -> inputArea.getChildren().remove(pane));

        then(VBox.getVgrow(promptRow)).isEqualTo(Priority.ALWAYS);
        then(prompt.getPrefHeight()).isEqualTo(Region.USE_COMPUTED_SIZE);
    }
}
