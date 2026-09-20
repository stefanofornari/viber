package ste.ai.viber.log;

import java.io.IOException;
import javafx.application.Platform;
import javafx.stage.Stage;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class LogViewerWindow extends Application {
    private final static String VIEWER_WINDOW = "logviewer.fxml";
    private final Logger LOG = Logger.getLogger(LogViewerWindow.class.getName());

    private Stage stage; // This will hold the reference created in start()
    private LogViewerHandler requestHandler;
    private LogViewerHandler responseHandler;

    @FXML
    private LogViewer requestLogViewer, responseLogViewer;

    // 1. Keep the constructor completely empty (or remove it entirely)
    public LogViewerWindow() {
        // DO NOT call Application.launch() here
    }

    @Override
    public void start(Stage stage) {
        // 2. Save the stage reference passed by JavaFX
        this.stage = stage;

        stage.setTitle("Viber - HTTP Logs");
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource(VIEWER_WINDOW));
            loader.setController(this);
            stage.setScene(new Scene(loader.load()));
            show();
        } catch (IOException x) {
            LOG.severe(() -> "error loading " + VIEWER_WINDOW);
            throw new RuntimeException(x);
        }
        stage.setOnCloseRequest(event -> cleanupHandlers());
    }

    @FXML
    private void initialize() {
        final Logger httpLogger = Logger.getLogger("dev.langchain4j.http.client.log");
        httpLogger.setLevel(java.util.logging.Level.ALL);

        // Fixed minor typo: (was double assigned requestLogViewer.main)
        requestLogViewer.main = this;

        requestHandler = new LogViewerHandler("dev.langchain4j.http.client.log", requestLogViewer);
        responseHandler = new LogViewerHandler("dev.langchain4j.http.client.log", responseLogViewer);

        requestHandler.setFilter(record -> record.getMessage().startsWith("HTTP request:"));
        responseHandler.setFilter(record -> record.getMessage().startsWith("HTTP response:"));

        httpLogger.addHandler(requestHandler);
        httpLogger.addHandler(responseHandler);

        LOG.finest(() -> "creating Scene and showing Stage");
    }

    public void show() {
        LOG.finest(() -> "show() called, isFxApplicationThread=" + Platform.isFxApplicationThread());

        // 3. Since start() initializes the stage, ensure we handle showing it properly
        if (Platform.isFxApplicationThread()) {
            if (stage != null) stage.show();
        } else {
            Platform.runLater(() -> {
                if (stage != null) stage.show();
            });
        }
    }

    private void cleanupHandlers() {
        Logger httpLogger = Logger.getLogger("dev.langchain4j.http.client.log");
        if (requestHandler != null) httpLogger.removeHandler(requestHandler);
        if (responseHandler != null) httpLogger.removeHandler(responseHandler);
    }

    public void cleanup() {
        Platform.runLater(() -> {
            cleanupHandlers();
            if (stage != null) stage.close();
        });
    }

    public void onLogClick(final String source, final String recordId) {
        System.out.println("source: %s, id: %s".formatted(source, recordId));
        if ("request".equals(source)) {
            responseLogViewer.webView.getEngine().executeScript(
                "highlight(document.getElementById(\"%s\"));".formatted(recordId)
            );
        } else {
            requestLogViewer.webView.getEngine().executeScript(
                "highlight(document.getElementById(\"%s\"));".formatted(recordId)
            );
        }
    }
}
