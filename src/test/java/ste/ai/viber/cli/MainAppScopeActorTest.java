package ste.ai.viber.cli;

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import picocli.CommandLine;
import ste.ai.viber.actor.ScopeActor;
import ste.ai.viber.cli.command.CLIOptions;

class MainAppScopeActorTest extends ApplicationTest {

    private MainAppController controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainAppWindow.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void app_contains_scope_agent_menu_item() {
        // Now you can safely check the menu item directly from the injected controller field
        then(controller.scopeAgentItem).isNotNull();
        then(controller.scopeAgentItem.getText()).isEqualTo("Scope");
        then(controller.scopePhaseIndicator).isNotNull();
    }

    @Test
    void create_scope_actor_returns_scope_actor() throws Exception {
        CLIOptions options = new CLIOptions();
        new CommandLine(options).parseArgs("--key", "test-key", "--endpoint", "https://test.com/v1", "--model", "gpt-4o");

        setField(controller, "options", options);

        ScopeActor actor = controller.createScopeActor();

        then(actor).isNotNull();
        then(actor).isInstanceOf(ScopeActor.class);
        then(actor.conversation()).isNotNull();
    }

    @Test
    void is_scope_phase_active_returns_false_initially() {
        then(controller.isScopePhaseActive()).isFalse();
    }

    @Test
    void is_scope_phase_active_returns_true_when_scope_actor_selected() throws Exception {
        setField(controller, "currentActorType", "ScopeActor");

        then(controller.isScopePhaseActive()).isTrue();
    }

    @Test
    void get_current_actor_type_returns_scope_actor_type() throws Exception {
        setField(controller, "currentActorType", "ScopeActor");

        then(controller.getCurrentActorType()).isEqualTo("ScopeActor");
    }

    @Test
    void scope_phase_indicator_field_exists() throws Exception {
        List<Field> fields = List.of(MainAppController.class.getDeclaredFields());

        then(fields).anyMatch(f -> f.getName().equals("scopeAgentItem"));
        then(fields).anyMatch(f -> f.getName().equals("scopePhaseIndicator"));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = MainAppController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}