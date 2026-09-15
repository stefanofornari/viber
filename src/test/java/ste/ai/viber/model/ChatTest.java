package ste.ai.viber.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.thenThrownBy;

class ChatTest {

    @Test
    void throws_on_null_prompt() {
        thenThrownBy(() -> new Chat(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("prompt must not be null");
    }
}
