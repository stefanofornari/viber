package ste.ai.viber.cli;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.CheckMenuItem;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.util.WaitForAsyncUtils;
import ste.ai.viber.cli.command.CLIOptions;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.renderer.JavaFxRenderer;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.BDDAssertions.then;

@ExtendWith(TestNameLogger.class)
class MainAppControllerTest extends org.testfx.framework.junit5.ApplicationTest {

    @Override
    public void start(Stage stage) {
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
    void controller_actor_menu_has_fxactor_and_langchain4j_options() {
        MainAppController controller = loadController(null);
        WaitForAsyncUtils.waitForFxEvents();

        MenuBar menuBar = (MenuBar) controller.getRoot().lookup(".menu-bar");
        Menu actorMenu = menuBar.getMenus().stream()
            .filter(m -> "Actor".equals(m.getText()))
            .findFirst()
            .orElseThrow();

        then(actorMenu.getItems()).hasSize(2);
        CheckMenuItem fxActorItem = (CheckMenuItem) actorMenu.getItems().get(0);
        CheckMenuItem langChainItem = (CheckMenuItem) actorMenu.getItems().get(1);
        then(fxActorItem.getText()).isEqualTo("Manual");
        then(langChainItem.getText()).isEqualTo("LLM");
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
    void fxml_injects_conversation_renderer() {
        MainAppController controller = loadController(null);
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.getRoot().lookup(".conversation")).isNotNull();
    }

    @Test
    void fxml_renderer_displays_conversation() {
        Conversation conversation = new Conversation().addChat(new ste.ai.viber.model.Chat(new PromptMessage("Hello")));
        MainAppController controller = loadController(conversation, null);
        WaitForAsyncUtils.waitForFxEvents();

        then(controller.getRoot().lookup(".conversation")).isNotNull();
    }

    private javafx.scene.control.MenuItem getActorMenuItem(MainAppController controller, int index) {
        MenuBar menuBar = (MenuBar) controller.getRoot().lookup(".menu-bar");
        Menu actorMenu = menuBar.getMenus().stream()
            .filter(m -> "Actor".equals(m.getText()))
            .findFirst()
            .orElseThrow();
        return actorMenu.getItems().get(index);
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
}
