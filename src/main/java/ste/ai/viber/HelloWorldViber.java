package ste.ai.viber;

import dev.langchain4j.model.chat.ChatModel;
import java.util.List;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.renderer.StringRenderer;
import ste.ai.viber.tools.InputTool;

public class HelloWorldViber implements Viber {
    private final ChatModel chatModel;

    public HelloWorldViber(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public void start() {
        LangChain4jActor actor = new LangChain4jActor(
            chatModel,
            List.<ste.ai.viber.tools.Tool>of(new InputTool()),
            "use mock 'hello world.2.txt'\n" +
            "To greet the user, use the InputTool with the prompt 'What is your name?'. execute tool input with arguments: What's your name?",
            null
        );

        Chat chat = new Chat(new PromptMessage("greet me by my name"));
        actor.chat(chat);

        Conversation conversation = actor.conversation();
        StringRenderer renderer = new StringRenderer();
        renderer.render(conversation);
    }
}
