package ste.ai.viber.test;

import atlantafx.base.theme.NordLight;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Applies to test scenes the same theme and stylesheet the application uses,
 * so that CSS lookups (e.g. -color-fg-default, -fx-viber-color-bg) resolve,
 * and provides helpers to make UI interactions more reliable.
 */
public final class FXTestSupport {

    private static final String VIBER_CSS = "/ste/ai/viber/ui/viber.css";

    private FXTestSupport() {
    }

    /** Must be called on the JavaFX Application Thread. */
    public static Scene styled(final Scene scene) {
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
        scene.getStylesheets().add(FXTestSupport.class.getResource(VIBER_CSS).toExternalForm());
        return scene;
    }

    /** Brings the stage to front and gives it focus so mouse/key events are delivered to it. */
    public static void focus(final Stage stage) {
        WaitForAsyncUtils.asyncFx(() -> {
            stage.toFront();
            stage.requestFocus();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Waits until the node is showing and laid out, so it can be safely clicked. */
    public static void waitUntilReady(final Node node) {
        try {
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () ->
                node.getScene() != null
                && node.getScene().getWindow() != null
                && node.getScene().getWindow().isShowing()
                && node.isVisible()
                && node.getLayoutBounds().getWidth() > 0
                && node.getLayoutBounds().getHeight() > 0
            );
        } catch (final java.util.concurrent.TimeoutException x) {
            throw new IllegalStateException("node not ready: " + node, x);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }
}
