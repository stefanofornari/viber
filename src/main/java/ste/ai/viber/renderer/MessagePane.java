package ste.ai.viber.renderer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TitledPane;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.Initializable;
import org.w3c.dom.Document;
import io.github.raghultech.markdown.javafx.preview.MarkdownWebView;
import ste.ai.viber.cli.ViberApplication;
import ste.ai.viber.model.ChatMessage;
import ste.ai.viber.model.ErrorMessage;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.model.ReplyMessage;
import ste.ai.viber.model.ThoughtMessage;
import ste.ai.viber.model.ToolInvocationMessage;
import ste.ai.viber.model.ToolExecutionMessage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MessagePane extends TitledPane implements Initializable {

    @FXML
    private WebView webView;

    private MarkdownWebView markdownPreview;
    private ChangeListener<Document> documentListener;

    public MessagePane() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MessagePane.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.markdownPreview = new MarkdownWebView(this.webView, "", ViberApplication.HOST);
    }

    public void message(ChatMessage message) {
        String content = message.content();
        String type = messageTypeFor(message);
        setText(message.role().name() + " / " + type);
        getStyleClass().removeAll("message-prompt", "message-reply", "message-thought", "message-tool", "message-error");
        getStyleClass().add(styleClassFor(message));

        markdownPreview.setContent(content);

        WebEngine engine = webView.getEngine();
        if (documentListener != null) {
            engine.documentProperty().removeListener(documentListener);
        }
        engine.documentProperty().addListener(documentListener = (obs, oldDoc, newDoc) -> {
            if (newDoc != null) {
                Platform.runLater(() -> {
                    try {
                        final Object height = engine.executeScript("document.body.scrollHeight");
                        System.out.println("height: " + height);
                        if (height instanceof Number h) {
                            webView.setPrefHeight(h.doubleValue());
                        }
                    } catch (Exception e) {
                        webView.setPrefHeight(40);
                    }
                });
            }
        });
    }

    public void appendMessage(ChatMessage message) {
        String current = markdownPreview.getContent();
        String newContent;
        if (current == null || current.isEmpty()) {
            newContent = message.content();
        } else {
            newContent = current + "\n\n" + message.content();
        }
        markdownPreview.setContent(newContent);
    }

    public static String messageTypeFor(ChatMessage message) {
        return switch (message) {
            case PromptMessage ignored -> "PROMPT";
            case ReplyMessage ignored -> "REPLY";
            case ThoughtMessage ignored -> "THOUGHT";
            case ToolInvocationMessage ignored -> "TOOL_EXECUTION_REQUEST";
            case ToolExecutionMessage ignored -> "TOOL_EXECUTION_RESPONSE";
            case ErrorMessage ignored -> "ERROR";
        };
    }

    private static String styleClassFor(ChatMessage message) {
        return switch (message) {
            case PromptMessage ignored -> "message-prompt";
            case ReplyMessage ignored -> "message-reply";
            case ThoughtMessage ignored -> "message-thought";
            case ToolInvocationMessage ignored -> "message-tool";
            case ToolExecutionMessage ignored -> "message-tool";
            case ErrorMessage ignored -> "message-error";
        };
    }
}
