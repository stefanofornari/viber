package ste.ai.viber.actor;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;


import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.json.JSONObject;
import ste.lazyjava.safe.Safe;
import ste.llm.chat.model.Chat;
import ste.llm.chat.model.ChatMessage;
import ste.llm.chat.model.Conversation;
import ste.llm.chat.model.PromptMessage;
import ste.llm.chat.model.ReplyMessage;
import ste.llm.chat.model.SystemMessage;
import ste.llm.chat.model.ThoughtMessage;
import ste.llm.chat.model.ToolExecutionMessage;
import ste.llm.chat.model.ToolInvocationMessage;
import ste.llm.tools.InvocationJSONObject;
import ste.llm.tools.Tool;

public class LangChain4jActor implements Actor, Safe {
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
                        ToolInvocationMessage toolMsg = new ToolInvocationMessage(
                            new InvocationJSONObject(request.name(), new JSONObject(request.arguments())));
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
