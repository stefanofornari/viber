# Viber Scope Overview

## Project Name
- Viber

## Version
- 0.0.0-SNAPSHOT

## Group ID / Artifact ID
- ste.ai:viber

## Overall Description

Viber is a conversational data model for interacting with an LLM. It defines how a vibe chat is conducted and the data model behind it, delegating real interaction with the user and with the LLM to external modules.

The model is built around a `Viber`-`Actor` conversation pattern. A conversation is a sequence of `chat`s, where each `chat` is a sequence of related `message`s. A typical example is the interaction between a user and an LLM: the user starts the conversation by submitting a prompt, playing the `Viber` role; the LLM answers, playing the `Actor` role. These are conventional names — the same actor can potentially play as `Viber` in some conversations and as `Actor` in others.

Viber provides the following submodules for different types of user interaction:

- **VibeChatFX** — JavaFX UI
- **VibeChatNB** — NetBeans Module built on top of VibeChatFX
- **VibeChatCLI** — Terminal/CLI user interface

Viber provides the following submodules to interact with the chat counterpart (e.g., an LLM):

- **STDIOActor** — writes messages from the counterpart to `stdout` and reads messages to return from `stdin`
- **LangChain4jActor** — interacts with a LangChain4j model

### Conversations and Chats

A conversation is a sequence of `chat`s. Each `chat` is a sequence of `message`s that are related in some way. Visually, a conversation can be thought of as chats that develop left to right, while each chat develops top to bottom.

For example, a user can start a chat to develop two features of a product: feature A and feature B. The developer starts the conversation by providing an overview of both features and confirming the requirements with the LLM. They then start implementing feature A, which requires the user to provide the prompt and a number of back-and-forth exchanges until the feature is done. When feature A is complete, they start implementing feature B. This conversation is made of three chats:

1. The requirements definition session
2. Implementation of feature A
3. Implementation of feature B

### Messages

A message is any exchange between the two actors chatting. It can be of different types, for instance:

- **prompt** — usually text or image
- **reply** — usually text or markdown
- **thought** — usually text
- **tool execution request** — tool's name and arguments
- **tool execution response** — tool's output

## In-Scope Capabilities

- Conversation / Chat / Message model implementation
- Model persistence
- UI to represent a conversation as text and in a JavaFX control
- JavaFX component for embedding Viber in host JavaFX applications
- `VibeChatCLI` terminal interface
- `STDIOActor` for standard input/output interaction
- `LangChain4jActor` for LangChain4j integration
- `VibeChatNB` NetBeans module (post-MVP, after the pure JavaFX MVP is complete)

## Out-of-Scope / Future Enhancements

- LLM actor implementations beyond `STDIOActor` and `LangChain4jActor`
- LLM-specific features

## Technical Context & Infrastructure

- **Development Toolchain**: Java, JavaFX, CommonsFX, AtlantaFX, Maven, JUnit 5, TestFX, AssertJ, Headless test execution support, TDD-oriented workflow
- **Target Systems/Platforms**: JavaFX desktop applications, Terminal application, Demo application for local desktop execution, Host applications embedding the component
- **Project Reference Docs**:
  - Coding Standard: `uzz/specs/coding-standard.md`
  - Development Framework: `uzz/specs/development-framework.md`
