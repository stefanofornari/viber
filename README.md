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

## Running the CLI

```bash
# Echo mode (no LLM)
java -jar target/viber-0.0-SNAPSHOT.jar --echo

# Real LLM
java -jar target/viber-0.0-SNAPSHOT.jar \
  --key sk-... \
  --endpoint https://api.openai.com/v1 \
  --model gpt-4o-mini \
  --system-prompt "You are a helpful assistant."
```

## Status

- [x] US-000006: CLI Interface

## Development

- Java 21
- Maven
- JUnit 5 + AssertJ + TestFX
- LangChain4j
- picocli
