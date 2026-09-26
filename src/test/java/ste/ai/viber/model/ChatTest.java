package ste.ai.viber.model;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

class ChatTest {

    @Test
    void throws_on_null_prompt() {
        thenThrownBy(() -> new Chat(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("prompt must not be null");
    }

    @Test
    void concurrent_modification_when_messages_added_during_iteration() throws Exception {
        Chat chat = new Chat(new PromptMessage("Hello"));

        CyclicBarrier barrier = new CyclicBarrier(2);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Thread adder = new Thread(() -> {
            try {
                barrier.await();
                for (int i = 0; i < 5000; i++) {
                    chat.addMessage(new PromptMessage("msg-" + i));
                }
            } catch (Throwable t) {
                failure.compareAndSet(null, t);
            }
        });
        adder.setDaemon(true);
        adder.start();

        try {
            barrier.await();
            for (int i = 0; i < 500; i++) {
                int count = 0;
                for (ChatMessage msg : chat.messages()) {
                    count++;
                }
                then(count).isPositive();
            }
            adder.join(5000);
        } catch (Throwable t) {
            if (failure.get() == null) {
                failure.compareAndSet(null, t);
            }
        }

        then(failure.get()).isNull();
    }
}
