# Development Guide

This document describes the completed user stories from the **model** and **development** domains and how to use their outcomes as a developer.

---

## 1. Core Conversational Data Model (US-000001)

### Overview

The `ste.ai.viber.model` package provides the foundational data structures for representing Viber-Actor conversations.

### Key Classes

- **`Conversation`** - An ordered sequence of `Chat` instances, optionally with a `SystemMessage`.
- **`Chat`** - A single conversation thread, initialized with a `PromptMessage` and containing an ordered list of `ChatMessage` instances.
- **`Message`** - A record holding `Role`, `MessageType`, and `content`.
- **`ChatMessage`** - A sealed interface implemented by:
  - `PromptMessage` (role: VIBER)
  - `ReplyMessage` (role: ACTOR)
  - `ThoughtMessage` (role: ACTOR)
  - `ToolInvocationMessage` (role: ACTOR)
  - `ToolExecutionMessage` (role: VIBER)
  - `ErrorMessage` (role: ACTOR)
- **`SystemMessage`** - Optional system-level instructions for the conversation.
- **`Role`** - Enum with values `VIBER` and `ACTOR`.

### Usage

```java
import ste.ai.viber.model.*;

Conversation conversation = new Conversation()
    .systemMessage(new SystemMessage("You are a helpful assistant."));

Chat chat = new Chat(new PromptMessage("Hello, how are you?"));
chat.addMessage(new ReplyMessage("I'm doing well, thank you!"));
chat.addMessage(new ThoughtMessage("The user seems polite today."));

conversation.addChat(chat);

// Access chats and messages
conversation.chats().forEach(c -> c.messages().forEach(msg -> {
    System.out.println(msg.role() + ": " + msg.content());
}));
```

### Fluent API

Both `Conversation` and `Chat` support fluent usage:

```java
Conversation conversation = new Conversation()
    .addChat(new Chat(new PromptMessage("First prompt")))
    .addChat(new Chat(new PromptMessage("Second prompt")))
    .systemMessage(new SystemMessage("System instructions"));
```

---

## 2. Embeddable JavaFX Component (US-000005)

### Overview

`GUIRenderer` is a reusable JavaFX component that renders conversations. It extends `ScrollPane` and loads its UI from FXML. It never creates or manages a `Stage`; the host application is responsible for embedding it.

### Class

- **`ste.ai.viber.renderer.GUIRenderer`** - extends `ScrollPane`, implements `Renderer`.

### Usage in a Host Application

```java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.model.ReplyMessage;
import ste.ai.viber.renderer.GUIRenderer;

public class HostApp extends Application {
    @Override
    public void start(Stage stage) {
        GUIRenderer renderer = new GUIRenderer();

        Conversation conversation = new Conversation()
            .addChat(new Chat(new PromptMessage("Hello")));

        renderer.render(conversation);

        BorderPane root = new BorderPane();
        root.setCenter(renderer);

        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("Host Application");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

### Rendering Methods

The `Renderer` interface (implemented by `GUIRenderer`) provides three overloads:

- `render(Conversation)` - Clears and renders all chats as tabs.
- `render(Chat)` - Appends a new chat tab to the existing conversation.
- `render(ChatMessage)` - Appends a message to the current (last) chat.

```java
// Render a full conversation
renderer.render(conversation);

// Append a new chat
renderer.render(new Chat(new PromptMessage("New topic")));

// Append a message to the current chat
renderer.render(new ReplyMessage("A reply"));
```

### Thread Safety

All rendering methods marshal updates to the JavaFX Application Thread via `Platform.runLater()`, so they are safe to call from background threads.

---

## 3. Dev Mode CLI Flag (US-000017)

### Overview

The CLI accepts a `--gui` flag that starts the standalone GUI application with embedded dev tools. It replaces the previously removed `--dev` and `--fx-render` flags.

### Usage

```bash
java -jar viber.jar --gui --key YOUR_API_KEY --model gpt-4o-mini
```

The `--gui` flag:
- Is optional
- Does not interfere with existing flags (`--show-log`, `--echo`, `--key`, `--endpoint`, `--model`, `--system-prompt`)
- Is exposed via `CLIOptions.isGui()`

### Programmatic Access

```java
CLIOptions options = new CLIOptions();
CommandLine cmd = new CommandLine(options);
cmd.parseArgs(args);

if (options.isGui()) {
    // Launch GUI mode
}
```

---

## 4. Dev Mode Conversation Editor (US-000016)

### Overview

When the application is started with `--gui`, a **Dev Mode** window opens adjacent to the main window. It provides a text area and button bar for manually adding chats and messages to a conversation, enabling interactive testing of the JavaFX rendering and model behavior.

### Features

- Multi-line text input on top
- Button bar with actions: **Add Chat**, **Add Reply**, **Add Thought**, **Add Tool Execution**, **Add Tool Reply**, **Add Error**
- Buttons are disabled when the text area is empty
- Dev window appears adjacent to the main application window, both centered on screen
- Opens only after the main window is showing

### How to Use

1. Start the application with `--gui`
2. The dev window opens automatically next to the main window
3. Type content in the text area
4. Click an action button to create the corresponding message type in the currently selected chat

### Classes

- **`DevModeWindow`** - Manages the dev mode `Stage` and positioning.
- **`DevModeController`** - Handles button actions and delegates to the `FXActor`:
  - `handleAddChat()` - Creates a new `Chat` with a `PromptMessage`
  - `handleAddReply()` - Appends a `ReplyMessage` via `fxActor.addReply(text)`
  - `handleAddThought()` - Appends a `ThoughtMessage` via `fxActor.addThought(text)`
  - `handleAddToolExecution()` - Appends a `ToolInvocationMessage` via `fxActor.addToolExecution(text)`
  - `handleAddToolReply()` - Appends a `ToolExecutionMessage` via `fxActor.addToolReply(text)`
  - `handleAddError()` - Appends an `ErrorMessage` via `fxActor.addError(text)`

---

## 5. Embeddable JavaFX Component via FXML (US-000023)

### Overview

`GUIRenderer` can be embedded directly in FXML files, enabling declarative composition of the conversation UI alongside other JavaFX controls.

### FXML Usage

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.layout.BorderPane?>
<?import ste.ai.viber.renderer.GUIRenderer?>

<BorderPane xmlns="http://javafx.com/javafx/22" xmlns:fx="http://javafx.com/fxml/1">
    <center>
        <GUIRenderer fx:id="conversation" />
    </center>
</BorderPane>
```

### Controller Usage

```java
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.renderer.GUIRenderer;

import java.net.URL;
import java.util.ResourceBundle;

public class HostController implements Initializable {

    @FXML
    private GUIRenderer conversation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Conversation model = new Conversation()
            .addChat(new Chat(new PromptMessage("Hello from FXML!")));
        conversation.render(model);
    }
}
```

### Lifecycle

The component participates in the standard FXML lifecycle. Its `initialize()` method (invoked by `FXMLLoader`) sets up the internal UI, loads `ConversationPane.fxml`, and applies the stylesheet. The component is ready to render immediately after FXML loading completes.

---

## Summary

| Feature | Key Classes / Files | How to Use |
|---------|-------------------|------------|
| Core Data Model | `Conversation`, `Chat`, `ChatMessage` (and subclasses) | Build conversations programmatically using the fluent API |
| Embeddable JavaFX Component | `GUIRenderer` | Instantiate and add to any JavaFX scene graph; call `render()` to display conversations |
| Dev Mode CLI Flag | `CLIOptions` (`--gui`) | Pass `--gui` on the command line to launch GUI mode |
| Dev Mode Conversation Editor | `DevModeWindow`, `DevModeController` | Opens automatically with `--gui`; use the button bar to inject messages |
| FXML Embedding | `GUIRenderer` | Declare `<GUIRenderer fx:id="..."/>` in FXML and reference it from the controller |
