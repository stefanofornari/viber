package ste.ai.viber;

import org.junit.jupiter.api.Test;
import ste.ai.model.DummyChatModel;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withTextFromSystemIn;
import static org.assertj.core.api.BDDAssertions.then;

class HelloWorldViberTest {

    @Test
    void starts_session_and_renders_hello_world_conversation() throws Exception {
        String name = "Alice";

        DummyChatModel dummyModel = new DummyChatModel();
        dummyModel.toolChoice = dev.langchain4j.model.chat.request.ToolChoice.AUTO;

        String[] output = new String[1];
        withTextFromSystemIn(name)
            .execute(() -> {
                output[0] = tapSystemOut(() -> {
                    Viber viber = new HelloWorldViber(dummyModel);
                    viber.start();
                });
            });

        String actual = output[0];
        String expected = "What's your name?\n" +
            "--- System ---\n" +
            "  use mock 'hello world.2.txt'\n" +
            "To greet the user, use the InputTool with the prompt 'What is your name?'. execute tool input with arguments: What's your name?\n" +
            "--- Chat 1 ---\n" +
            "  VIBER/PROMPT> greet me by my name\n" +
            "  ACTOR/TOOL_EXECUTION_REQUEST[TOOL] ask for the name\n" +
            "  VIBER/TOOL_EXECUTION_RESPONSE[OUT] " + name + "\n" +
            "  ACTOR/REPLY: Hello " + name + "!\n" +
            "\n";

        then(actual).isEqualToIgnoringNewLines(expected);
    }
}
