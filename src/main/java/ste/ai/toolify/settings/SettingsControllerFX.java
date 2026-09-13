package ste.ai.toolify.settings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Stage;

public final class SettingsControllerFX {

    public final BooleanProperty changed = new SimpleBooleanProperty(false);
    private Stage dialogStage;

    public void dialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

}
