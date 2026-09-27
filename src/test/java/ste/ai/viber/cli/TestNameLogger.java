package ste.ai.viber.cli;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestNameLogger implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) {
        System.out.println(">>> TEST START: " + context.getDisplayName());
        System.out.flush();
    }
}
