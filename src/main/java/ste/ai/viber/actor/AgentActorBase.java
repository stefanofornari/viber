package ste.ai.viber.actor;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.service.TokenStream;

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
import ste.llm.chat.model.ThoughtMessage;
import ste.llm.chat.model.ToolExecutionMessage;
import ste.llm.chat.model.ToolInvocationMessage;
import ste.llm.tools.InvocationJSONObject;


public abstract class AgentActorBase implements Actor, Safe {

    protected final Conversation conversation;

    protected AgentActorBase() {
        this.conversation = new Conversation();
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
        TokenStream tokenStream = createTokenStream(prompt.content());
        handleTokenStream(tokenStream, chat, onMessage);
    }

    protected abstract TokenStream createTokenStream(String userMessage);

    private void handleTokenStream(TokenStream tokenStream, Chat chat, Consumer<ChatMessage> onMessage) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicBoolean partialReceived = new AtomicBoolean(false);
        StringBuilder replyBuffer = new StringBuilder();
        StringBuilder thinkingBuffer = new StringBuilder();

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
                        final ToolInvocationMessage toolMsg = new ToolInvocationMessage(
                            new InvocationJSONObject(request.name(), new JSONObject(request.arguments()))
                        );

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