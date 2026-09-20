package ste.ai.viber.log;

import java.io.File;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import static ste.ai.viber.util.Utils.ifNull;


public class LogViewer extends Pane {

    private static final Logger LOG = Logger.getLogger(LogViewer.class.getName());

    public final WebView webView = new WebView();

    public LogViewerWindow main;

    private final String logViewerBaseDir;

    private static final String DEFAULT_LOGVIEWER_BASE_DIR = "etc/resources/logviewer";

    public LogViewer() {
        this(DEFAULT_LOGVIEWER_BASE_DIR);
    }

    public LogViewer(final String logViewerBaseDir) {
        this.logViewerBaseDir = logViewerBaseDir;
        this.getChildren().add(webView);

        webView.prefWidthProperty().bind(this.widthProperty());
        webView.prefHeightProperty().bind(this.heightProperty());

        final WebEngine engine = webView.getEngine();

        engine.setOnAlert((event) -> {
            LOG.finest(() -> "WebView alert: " + event.getData());
        });
        engine.setOnError((event) -> {
            LOG.finest(() -> "WebView error: " + event.getMessage());
        });
        engine.getLoadWorker().stateProperty().addListener(
            (obj, was, is) -> {
                LOG.finest(() -> "WebView %s/%s/%s".formatted(String.valueOf(obj), was, is));
                if ("SUCCEEDED".equals(String.valueOf(is))) {
                    LOG.finest("WebView page loaded successfully");
                    Platform.runLater(() -> {
                        JSObject window = (JSObject) engine.executeScript("window");
                        window.setMember("mainController", main);
                    });
                }
            }
        );
    }

    /**
     * JavaFX calls this method automatically AFTER the constructor finishes,
     * passing the value defined in userData="..." from your FXML.
     */
    @Override
    public void setUserData(final Object value) {
        super.setUserData(value); // Keep standard JavaFX behavior intact

        final WebEngine engine = webView.getEngine();
        final File f = new File(logViewerBaseDir, "logviewer.html");
        final String url = f.toURI().toString() + "?role=" + String.valueOf(value);

        ifNull(
            url,
            () -> LOG.severe(() -> "resource not found: %s".formatted(url)),
            () -> engine.load(url)
        );
    }

    public void clear() {
        webView.getEngine().executeScript("clear();");
    }

    public String getText() {
        return (String) webView.getEngine().executeScript("document.documentElement.outerHTML");
    }

    public void log(final String record) {
        LOG.finest(() -> "LogViewer.log called with record length=" + record.length());
        Platform.runLater(() -> {
            try {
                LOG.finest(() -> "executing JS log() with record: " + record);
                webView.getEngine().executeScript("log(%s);".formatted(record));
            } catch (Throwable x) {
                LOG.log(java.util.logging.Level.WARNING, "Failed to execute JS log()", x);
                x.printStackTrace();
            }
        });
    }
}
