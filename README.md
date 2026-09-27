# Viber

Viber is a conversational data model for interacting with an LLM. It defines how a Viber conversation is conducted and the data model behind it, delegating real interaction with the user and with the LLM to external modules.

## Modules

- **ViberFX** — JavaFX UI
- **ViberNB** — NetBeans Module built on top of ViberFX
- **ViberCLI** — Terminal/CLI user interface
- **STDIOActor** — writes messages from the counterpart to `stdout` and reads messages to return from `stdin`
- **LangChain4jActor** — interacts with a LangChain4j model

## Building

Requires Java 21 and Maven.

```bash
mvn package
```

This produces an uber JAR at `target/viber-0.0-SNAPSHOT.jar`.

## Running the Application

```bash
# Standalone GUI application (no API key needed)
mvn javafx:run

# Standalone GUI application with explicit flag
mvn javafx:run -Djavafx.args="--gui"

# CLI with real LLM (requires API key)
mvn javafx:run -Djavafx.args="--key sk-..."

# CLI with Echo mode (no LLM)
mvn javafx:run -Djavafx.args="--echo"

# Dev mode (manual message editor)
mvn javafx:run -Djavafx.args="--dev"

# CLI + FX rendering (CLI with conversation window)
mvn javafx:run -Djavafx.args="--key sk-... --fx-render"
```

### Launch Behavior

- **No flags / `--gui`**: Launches the standalone JavaFX GUI application with FXActor as the default actor. No API key required.
- **`--key`**: Launches the CLI with LangChain4jActor connected to the specified LLM provider.
- **`--echo`**: Launches the CLI with StdInStdOutActor, echoing user input without calling an LLM.
- **`--dev`**: Opens the JavaFX dev mode window for manually adding chats and messages.
- **`--fx-render`**: Runs the CLI while also rendering the conversation in a JavaFX window.

You can also run the packaged JAR directly:

```bash
java -jar target/viber-0.0-SNAPSHOT.jar --gui
java -jar target/viber-0.0-SNAPSHOT.jar --echo
java -jar target/viber-0.0-SNAPSHOT.jar --dev
```

## Status

- [x] US-000006: CLI Interface

## Development

- Java 21
- Maven
- JUnit 5 + AssertJ + TestFX
- LangChain4j
- picocli
