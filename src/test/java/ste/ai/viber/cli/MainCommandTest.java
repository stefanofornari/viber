package ste.ai.viber.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import ste.ai.viber.cli.command.CliOptions;

import static org.assertj.core.api.BDDAssertions.then;

class MainCommandTest {

    @Test
    void parses_default_options() {
        CliOptions options = parse(new String[]{"--key", "x"});

        then(options.endpoint()).isEqualTo("https://api.openai.com/v1");
        then(options.model()).isEqualTo("gpt-4o-mini");
        then(options.systemPrompt()).isEqualTo("You are a helpful assistant.");
        then(options.isEcho()).isFalse();
    }

    @Test
    void parses_all_options() {
        CliOptions options = parse(new String[]{
            "--key", "secret",
            "--endpoint", "http://localhost:8080/v1",
            "--model", "custom-model",
            "--system-prompt", "Be concise.",
            "--echo"
        });

        then(options.key()).isEqualTo("secret");
        then(options.endpoint()).isEqualTo("http://localhost:8080/v1");
        then(options.model()).isEqualTo("custom-model");
        then(options.systemPrompt()).isEqualTo("Be concise.");
        then(options.isEcho()).isTrue();
    }

    private CliOptions parse(String... args) {
        CliOptions options = new CliOptions();
        new CommandLine(options).parseArgs(args);
        return options;
    }
}
