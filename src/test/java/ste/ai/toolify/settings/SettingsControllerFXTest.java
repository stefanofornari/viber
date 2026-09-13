package ste.ai.toolify.settings;

import static org.junit.jupiter.api.Assertions.*;

import javafx.beans.property.BooleanProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ste.ai.toolify.settings.SettingsControllerFX;

public class SettingsControllerFXTest {

    private SettingsControllerFX controller;

    @BeforeEach
    void setUp() {
        controller = new SettingsControllerFX();
    }

    @Test
    void changed_property_exists_and_initially_false() {
        BooleanProperty changedProperty = controller.changed;
        assertNotNull(changedProperty);
        assertFalse(changedProperty.get());
    }

    @Test
    void changed_property_notifies_listeners() {
        BooleanProperty changedProperty = controller.changed;
        final boolean[] notified = {false};
        changedProperty.addListener((observable, oldValue, newValue) -> {
            notified[0] = true;
            assertEquals(false, oldValue);
            assertEquals(true, newValue);
        });
        changedProperty.set(true);
        assertTrue(notified[0]);
    }

}
