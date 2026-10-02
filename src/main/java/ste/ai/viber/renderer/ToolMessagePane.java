package ste.ai.viber.renderer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TitledPane;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import org.w3c.dom.Document;
import io.github.raghultech.markdown.javafx.preview.MarkdownWebView;
import ste.ai.viber.cli.ViberApplication;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ToolMessagePane extends TitledPane implements Initializable {

    @FXML
    private ToolInvocationPane invocationPane;

    @FXML
    private WebView resultWebView;

    private MarkdownWebView markdownPreview;
    private ChangeListener<Document> documentListener;
    private boolean invocationSet = false;

    public ToolMessagePane() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ToolMessagePane.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        getStyleClass().addAll("message", "message-tool");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.markdownPreview = new MarkdownWebView(this.resultWebView, "", ViberApplication.HOST);

        WebEngine engine = resultWebView.getEngine();
        engine.documentProperty().addListener(documentListener = (obs, oldDoc, newDoc) -> {
            if (newDoc != null) {
                Platform.runLater(() -> {
                    try {
                        final Object height = engine.executeScript("document.body.scrollHeight");
                        if (height instanceof Number h) {
                            resultWebView.setPrefHeight(h.doubleValue());
                        }
                    } catch (Exception e) {
                        resultWebView.setPrefHeight(40);
                    }
                });
            }
        });

        setMaxWidth(Double.MAX_VALUE);
        invocationPane.setMaxWidth(Double.MAX_VALUE);
        resultWebView.setMaxWidth(Double.MAX_VALUE);
    }

    public void invocation(String content) {
        invocationSet = true;
        invocationPane.rawContent(content);
        invocationPane.hideName();
        setText("ACTOR / " + invocationPane.toolName());
    }

    public void appendResult(String result) {
        if (!invocationSet) {
            setText("VIBER / " + invocationPane.toolName());
            invocationPane.setVisible(false);
        }
        String current = markdownPreview.getContent();
        String newContent = current == null || current.isEmpty() ? result : current + "\n\n" + result;
        markdownPreview.setContent(newContent);
    }
}
