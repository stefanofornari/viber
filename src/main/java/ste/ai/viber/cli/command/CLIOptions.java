package ste.ai.viber.cli.command;

import picocli.CommandLine;


public class CLIOptions {

    @CommandLine.Option(
        names = {"--key"},
        description = "API key for the LLM provider"
    )
    String key;

    @CommandLine.Option(
        names = {"--gui"},
        description = "Start the standalone GUI application"
    )
    boolean gui;

    public boolean isGui() {
        return gui;
    }

    @CommandLine.Option(
        names = {"--endpoint"},
        description = "Base URL of the LLM provider (default: OpenAI)",
        defaultValue = "https://api.openai.com/v1"
    )
    String endpoint;

    @CommandLine.Option(
        names = {"--model"},
        description = "Model name to use (default: gpt-4o-mini)",
        defaultValue = "gpt-4o-mini"
    )
    String model;

    @CommandLine.Option(
        names = {"--system-prompt"},
        description = "System prompt for the actor"
    )
    String systemPrompt = "You are a helpful assistant.";

    @CommandLine.Option(
        names = {"--show-log"},
        description = "Show a JavaFX window with HTTP request/response logs during the chat session"
    )
    boolean showLog;

    public boolean isShowLog() {
        return showLog;
    }

    @CommandLine.Option(
        names = {"--echo"},
        description = "Echo mode: actor repeats user input without calling an LLM"
    )
    boolean echo;

    public boolean isEcho() {
        return echo;
    }

    public String key() {
        return key;
    }

    public void key(String key) {
        this.key = key;
    }

    public String endpoint() {
        return endpoint;
    }

    public String model() {
        return model;
    }

    public String systemPrompt() {
        return systemPrompt;
    }
}
