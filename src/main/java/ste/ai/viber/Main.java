package ste.ai.viber;

import picocli.CommandLine;
import ste.ai.viber.cli.MainCommand;

public class Main {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new MainCommand()).execute(args);
        System.exit(exitCode);
    }
}
