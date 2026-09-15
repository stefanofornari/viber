package ste.ai.viber.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.thenThrownBy;

class PromptMessageTest {

    @Test
    void rejects_null_content() {
        thenThrownBy(() -> new PromptMessage(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("content must be non blank");
    }

    @Test
    void rejects_blank_content() {
        thenThrownBy(() -> new PromptMessage("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("content must be non blank");
    }
}
