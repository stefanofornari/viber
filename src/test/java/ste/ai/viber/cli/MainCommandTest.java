package ste.ai.viber.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import ste.ai.viber.cli.command.CLIOptions;

import static org.assertj.core.api.BDDAssertions.then;

class MainCommandTest {

    @Test
    void parses_default_options() {
        CLIOptions options = parse(new String[]{"--key", "x"});

        then(options.endpoint()).isEqualTo("https://api.openai.com/v1");
        then(options.model()).isEqualTo("gpt-4o-mini");
        then(options.systemPrompt()).isEqualTo("You are a helpful assistant.");
        then(options.isEcho()).isFalse();
        then(options.isShowLog()).isFalse();
    }

    @Test
    void exit_code_is_1_for_invalid_endpoint() {
        MainCommand command = new MainCommand();
        CommandLine commandLine = new CommandLine(command);
        int exitCode = commandLine.execute("--key", "x", "--endpoint", "htppps://invalid");
        then(exitCode).isEqualTo(1);
    }

    private CLIOptions parse(String... args) {
        CLIOptions options = new CLIOptions();
        new CommandLine(options).parseArgs(args);
        return options;
    }
}
