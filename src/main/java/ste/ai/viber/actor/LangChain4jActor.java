package ste.ai.viber.actor;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.ChatMessage;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.model.ReplyMessage;
import ste.ai.viber.model.SystemMessage;
import ste.ai.viber.model.ThoughtMessage;
import ste.ai.viber.model.ToolExecutionRequestMessage;
import ste.ai.viber.model.ToolExecutionResponseMessage;
import ste.ai.viber.tools.Tool;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import static ste.ai.viber.util.Utils.ifNotNull;
import static ste.ai.viber.util.Utils.requireNonNull;
import static ste.ai.viber.util.Utils.safe;

public class LangChain4jActor implements Actor {

    public interface ConversationConsumer extends Consumer {
        default void updated(final Conversation conversation) {
            accept(conversation);
        }
    }

    public interface ActorService {
        TokenStream chat(@UserMessage String userMessage);
    }

    private final ActorService service;
    private final Conversation conversation;
    private final ConversationConsumer updated;
    private final List<Tool> tools;

    public LangChain4jActor(StreamingChatModel streamingChatModel,
                            List<Tool> tools,
                            String systemPrompt,
                            ConversationConsumer updated) {
        requireNonNull(streamingChatModel, "streamingChatModel");
        requireNonNull(tools, "tools");
        requireNonNull(systemPrompt, "systemPrompt");
        if (systemPrompt.isBlank()) {
            throw new IllegalArgumentException("systemPrompt must not be blank");
        }
        if (tools.isEmpty()) {
            throw new IllegalArgumentException("tools must not be empty");
        }
        for (int i = 0; i < tools.size(); i++) {
            if (tools.get(i) == null) {
                throw new IllegalArgumentException("tools[%d] must not be null".formatted(i));
            }
        }

        this.conversation = new Conversation();
        this.updated = updated;
        this.tools = tools;

        conversation.systemMessage(new SystemMessage(systemPrompt));

        this.service = AiServices.builder(ActorService.class)
            .streamingChatModel(streamingChatModel)
            .tools(tools.toArray())
            .systemMessageProvider(o -> systemPrompt)
            .build();
    }

    public Conversation conversation() {
        return conversation;
    }

    public void chat(Chat chat, Consumer<ChatMessage> onMessage) {
        requireNonNull(chat, "chat");
        requireNonNull(onMessage, "onMessage");

        if (!conversation.chats().contains(chat)) {
            conversation.addChat(chat);
        }
        update();

        PromptMessage prompt = chat.prompt();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicBoolean partialReceived = new AtomicBoolean(false);
        StringBuilder replyBuffer = new StringBuilder();
        StringBuilder thinkingBuffer = new StringBuilder();

        TokenStream tokenStream = service.chat(prompt.content());
        tokenStream
            .onPartialResponse(text -> {
                partialReceived.set(true);
                replyBuffer.append(text);

                int newlineIndex;
                while ((newlineIndex = replyBuffer.indexOf("\n")) >= 0) {
                    String line = replyBuffer.substring(0, newlineIndex).trim();
                    if (!line.isEmpty()) {
                        ReplyMessage reply = new ReplyMessage(line);
                        chat.addMessage(reply);
                        onMessage.accept(reply);
                        update();
                    }
                    replyBuffer.delete(0, newlineIndex + 1);
                }
            })
            .onPartialThinking(thinking -> {
                thinkingBuffer.append(thinking.text());

                int newlineIndex;
                while ((newlineIndex = thinkingBuffer.indexOf("\n")) >= 0) {
                    String line = thinkingBuffer.substring(0, newlineIndex).trim();
                    if (!line.isEmpty()) {
                        ThoughtMessage thought = new ThoughtMessage(line);
                        chat.addMessage(thought);
                        onMessage.accept(thought);
                        update();
                    }
                    thinkingBuffer.delete(0, newlineIndex + 1);
                }
            })
            .onIntermediateResponse(response -> {
                if (response.aiMessage().hasToolExecutionRequests()) {
                    for (ToolExecutionRequest request : response.aiMessage().toolExecutionRequests()) {
                        String toolName = request.name();
                        String arguments = request.arguments();

                        String displayText = toolName;
                        if ("input".equals(toolName)) {
                            displayText = "ask for the name";
                        } else if (arguments != null && !arguments.equals("{}")) {
                            displayText = displayText + ": " + arguments;
                        }

                        ToolExecutionRequestMessage toolMsg = new ToolExecutionRequestMessage(displayText);
                        chat.addMessage(toolMsg);
                        onMessage.accept(toolMsg);
                        update();
                    }
                }
            })
            .onToolExecuted(execution -> {
                String toolResult = execution.result();
                ToolExecutionResponseMessage toolResponse = new ToolExecutionResponseMessage(toolResult);
                chat.addMessage(toolResponse);
                onMessage.accept(toolResponse);
                update();
            })
            .onCompleteResponse(response -> {
                String remainingThinking = thinkingBuffer.toString().trim();
                if (!remainingThinking.isEmpty()) {
                    ThoughtMessage thought = new ThoughtMessage(remainingThinking);
                    chat.addMessage(thought);
                    onMessage.accept(thought);
                    update();
                }

                if (!partialReceived.get()) {
                    String finalReply = response.aiMessage().text();
                    if (finalReply != null && !finalReply.isBlank()) {
                        ReplyMessage reply = new ReplyMessage(finalReply);
                        chat.addMessage(reply);
                        onMessage.accept(reply);
                        update();
                    }
                } else {
                    String remaining = replyBuffer.toString().trim();
                    if (!remaining.isEmpty()) {
                        ReplyMessage reply = new ReplyMessage(remaining);
                        chat.addMessage(reply);
                        onMessage.accept(reply);
                        update();
                    }
                }

                safe(response.metadata().tokenUsage(), (usage) -> chat.tokenUsage(usage));

                latch.countDown();
            })
            .onError(err -> {
                error.set(err);
                latch.countDown();
            })
            .start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while streaming chat", e);
        }

        if (error.get() != null) {
            throw new RuntimeException("Streaming chat failed", error.get());
        }
    }

    private void update() {
        ifNotNull(updated, () -> updated.updated(conversation));
    }
}
