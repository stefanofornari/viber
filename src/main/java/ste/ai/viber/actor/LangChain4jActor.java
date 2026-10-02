package ste.ai.viber.actor;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import ste.ai.viber.model.Chat;
import ste.ai.viber.model.ChatMessage;
import ste.ai.viber.model.Conversation;
import ste.ai.viber.model.PromptMessage;
import ste.ai.viber.model.ReplyMessage;
import ste.ai.viber.model.SystemMessage;
import ste.ai.viber.model.ThoughtMessage;
import ste.ai.viber.model.ToolInvocationMessage;
import ste.ai.viber.model.ToolExecutionMessage;
import ste.ai.viber.tools.Tool;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import static ste.ai.viber.util.Safe.ifNotNull;
import static ste.ai.viber.util.Safe.requireNonNull;
import static ste.ai.viber.util.Safe.requireFullyNonNull;
import static ste.ai.viber.util.Safe.safe;

public class LangChain4jActor implements Actor {
    public interface ActorService {
        TokenStream chat(@UserMessage String userMessage);
    }

    private final ActorService service;
    private final Conversation conversation;

    public LangChain4jActor(
        StreamingChatModel streamingChatModel,
        List<Tool> tools,
        String systemPrompt
    ) {
        requireNonNull(streamingChatModel, "streamingChatModel");
        requireFullyNonNull(tools, "tools");
        requireNonNull(systemPrompt, "systemPrompt");
        if (systemPrompt.isBlank()) {
            throw new IllegalArgumentException("systemPrompt must not be blank");
        }

        this.conversation = new Conversation();

        conversation.systemMessage(new SystemMessage(systemPrompt));

        this.service = AiServices.builder(ActorService.class)
            .streamingChatModel(streamingChatModel)
            .tools(tools.toArray())
            .systemMessageProvider(o -> systemPrompt)
            .build();
    }

    public LangChain4jActor(
        String endpoint,
        String apiKey,
        String model,
        String systemPrompt,
        List<Tool> tools
    ) {
        this(
            OpenAiStreamingChatModel.builder()
                .baseUrl(endpoint)
                .apiKey(apiKey)
                .modelName(model)
                .returnThinking(true)
                .logRequests(true)
                .logResponses(true)
                .build(),
            tools,
            systemPrompt
        );
    }

    @Override
    public Conversation conversation() {
        return conversation;
    }

    @Override
    public void chat(Chat chat, Consumer<ChatMessage> onMessage) {
        requireNonNull(chat, "chat");
        requireNonNull(onMessage, "onMessage");

        if (!conversation.chats().contains(chat)) {
            conversation.addChat(chat);
        }

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

                        ToolInvocationMessage toolMsg = new ToolInvocationMessage(displayText);
                        chat.addMessage(toolMsg);
                        onMessage.accept(toolMsg);
                    }
                }
            })
            .onToolExecuted(execution -> {
                String toolResult = execution.result();
                ToolExecutionMessage toolResponse = new ToolExecutionMessage(toolResult);
                chat.addMessage(toolResponse);
                onMessage.accept(toolResponse);
            })
            .onCompleteResponse(response -> {
                String remainingThinking = thinkingBuffer.toString().trim();
                if (!remainingThinking.isEmpty()) {
                    ThoughtMessage thought = new ThoughtMessage(remainingThinking);
                    chat.addMessage(thought);
                    onMessage.accept(thought);
                }

                if (!partialReceived.get()) {
                    safe(response.aiMessage().text(), finalReply -> {
                        if (!finalReply.isBlank()) {
                            ReplyMessage reply = new ReplyMessage(finalReply);
                            chat.addMessage(reply);
                            onMessage.accept(reply);
                        }
                    });
                } else {
                    String remaining = replyBuffer.toString().trim();
                    if (!remaining.isEmpty()) {
                        ReplyMessage reply = new ReplyMessage(remaining);
                        chat.addMessage(reply);
                        onMessage.accept(reply);
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

        ifNotNull(error.get(), () -> {
            throw new RuntimeException("Streaming chat failed", error.get());
        });
    }
}
