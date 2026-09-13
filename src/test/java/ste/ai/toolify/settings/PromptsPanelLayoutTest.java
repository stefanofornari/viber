package ste.ai.toolify.settings;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.within;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import ste.ai.toolify.settings.PromptsPanelController;

public class PromptsPanelLayoutTest extends ApplicationTest {

    private Map<String, String> prefs;
    private PromptsPanelController controller;
    private PreferencesFx preferencesFx;

    @Override
    public void start(Stage stage) {
        prefs = new HashMap<>();
        controller = new PromptsPanelController(prefs);

        preferencesFx = PreferencesFx.of(PromptsPanelLayoutTest.class,
            Category.of("Prompts",
                Group.of("Manage Your Prompts",
                    Setting.of("Table", controller.getView())
                )
            )
        ).persistWindowState(false);

        Region preferencesView = (Region) preferencesFx.getView();
        preferencesView.setMaxWidth(Double.MAX_VALUE);
        preferencesView.setPrefWidth(Double.MAX_VALUE);

        StackPane root = new StackPane(preferencesView);
        root.setPrefWidth(1024);
        root.setPrefHeight(768);

        stage.setMaxWidth(Double.MAX_VALUE);
        stage.setMaxHeight(Double.MAX_VALUE);

        stage.setScene(new Scene(root, 1024, 768));
        stage.show();
    }

    @Test
    public void table_takes_full_width_and_second_column_expands() {
        // Wait for layout to settle
        interact(() -> {});

        double tableWidth = controller.table.getWidth();

        then(tableWidth).as("Table should expand to fill available space").isGreaterThan(800.0);

        TableColumn<?, ?> nameCol = controller.table.getColumns().get(0);
        TableColumn<?, ?> promptCol = controller.table.getColumns().get(1);

        then(nameCol.getWidth()).isCloseTo(200.0, within(1.0));
        then(promptCol.getWidth()).as("Prompt column should expand to fill the table").isGreaterThan(tableWidth - 210.0);
    }
}
