package ste.ai.viber;

import ste.ai.viber.actor.LangChain4jActor;
import dev.langchain4j.model.chat.StreamingChatModel;
import java.util.List;
import ste.ai.viber.renderer.StringRenderer;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.Conversation;
import ste.llm.chat.model.PromptMessage;
import ste.llm.tools.InputTool;


public class HelloWorldViber implements Viber {
    private final StreamingChatModel chatModel;

    public HelloWorldViber(StreamingChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public void start() {
        LangChain4jActor actor = new LangChain4jActor(
            chatModel,
            List.<ste.llm.tools.Tool>of(new InputTool()),
            "use mock 'hello world.2.txt'\n" +
            "To greet the user, use the InputTool with the prompt 'What is your name?'. execute tool input with arguments: What's your name?"
        );

        Chat chat = new Chat(new PromptMessage("greet me by my name"));
        actor.chat(chat, msg -> {});

        Conversation conversation = actor.conversation();
        StringRenderer renderer = new StringRenderer();
        renderer.render(conversation);
    }
}
