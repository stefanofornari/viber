package ste.ai.viber.log;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LogViewerWindow {
    private static Stage stage;
    private static LogViewerWindow instance;
    private LogViewerHandler requestHandler;
    private LogViewerHandler responseHandler;
    private final Logger LOG = Logger.getLogger(LogViewerWindow.class.getName());

    @FXML
    private LogViewer requestLogViewer, responseLogViewer;

    public static synchronized void show() {
        Platform.runLater(() -> {
            if (instance == null) {
                instance = new LogViewerWindow();
                instance.start(new Stage());
            } else if (stage != null) {
                stage.show();
                stage.toFront();
            }
        });
    }

    private void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Viber - HTTP Logs");
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("logviewer.fxml"));
            loader.setController(this);
            stage.setScene(new Scene(loader.load(), 1200, 800));
            stage.setMinWidth(800);
            stage.setMinHeight(500);
            stage.show();
        } catch (IOException x) {
            LOG.severe(() -> "error loading logviewer.fxml");
            throw new RuntimeException(x);
        }
        stage.setOnCloseRequest(event -> cleanupHandlers());
        initializeHandlers();
    }

    private void initializeHandlers() {
        final Logger httpLogger = Logger.getLogger("dev.langchain4j.http.client.log");
        httpLogger.setLevel(Level.ALL);

        requestLogViewer.main = this;

        requestHandler = new LogViewerHandler("dev.langchain4j.http.client.log", requestLogViewer);
        responseHandler = new LogViewerHandler("dev.langchain4j.http.client.log", responseLogViewer);

        requestHandler.setFilter(record -> record.getMessage().startsWith("HTTP request:"));
        responseHandler.setFilter(record -> {
            final String msg = record.getMessage();
            return msg.startsWith("HTTP response:") || msg.startsWith("ServerSentEvent");
        });

        httpLogger.addHandler(requestHandler);
        httpLogger.addHandler(responseHandler);

        LOG.finest(() -> "creating Scene and showing Stage");
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
        String targetId = recordId;
        int dotIndex = recordId.indexOf('.');
        if (dotIndex >= 0 && recordId.startsWith("record-")) {
            targetId = recordId.substring(0, dotIndex);
        }

        System.out.println("source: %s, id: %s".formatted(source, recordId));
        if ("request".equals(source)) {
            responseLogViewer.webView.getEngine().executeScript(
                "highlight(document.getElementById(\"%s\"));".formatted(targetId)
            );
        } else {
            requestLogViewer.webView.getEngine().executeScript(
                "highlight(document.getElementById(\"%s\"));".formatted(targetId)
            );
        }
    }
}
