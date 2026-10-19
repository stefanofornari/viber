package ste.ai.viber.cli;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestNameLogger implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) {
        final String name = context.getDisplayName();
        System.out.printf("%s\n%s\n", name, "‾".repeat(name.length())); System.out.flush();
    }
}
