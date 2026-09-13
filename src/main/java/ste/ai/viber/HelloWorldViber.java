package ste.ai.viber;

import dev.langchain4j.model.chat.ChatModel;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.renderer.StringRenderer;

import java.util.List;

public class HelloWorldViber implements Viber {
    private final ChatModel chatModel;

    public HelloWorldViber(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public void start() {
        LangChain4jActor actor = new LangChain4jActor(
            chatModel,
            List.of(new ste.ai.viber.tools.InputTool()),
            "use mock 'hello world.2.txt'\n" +
            "To greet the user, use the InputTool with the prompt 'What is your name?'. execute tool input with arguments: What's your name?",
            null
        );

        actor.chat("greet me by my name");

        Conversation conversation = actor.conversation();
        StringRenderer renderer = new StringRenderer();
        renderer.render(conversation);
    }
}
